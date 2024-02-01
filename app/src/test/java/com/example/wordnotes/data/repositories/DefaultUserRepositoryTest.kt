package com.example.wordnotes.data.repositories

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.mocks.FakeDataStoreRepository
import com.example.wordnotes.mocks.FakeUserRemoteDataSource
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultUserRepositoryTest {
    private lateinit var userRemoteDataSource: FakeUserRemoteDataSource
    private lateinit var dataStoreRepository: FakeDataStoreRepository
    private lateinit var userRepository: DefaultUserRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun createRepository() {
        userRemoteDataSource = FakeUserRemoteDataSource()
        dataStoreRepository = FakeDataStoreRepository()
        userRepository = DefaultUserRepository(userRemoteDataSource, dataStoreRepository, testDispatcher)
    }

    @Test
    fun `sign up should successfully`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "333333")
        val newUser = (userRepository.signUp(user) as Result.Success).data
        assertThat(newUser).isEqualTo(user.copy(id = newUser.id))
        assertThat((userRepository.getUser() as Result.Success).data).isEqualTo(newUser)
        assertThat((dataStoreRepository.getUser() as Result.Success).data).isEqualTo(newUser)
    }

    @Test
    fun `sign up with existing user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        assertThat(userRepository.signUp(user) is Result.Error).isTrue()
        assertThat(userRepository.getUser() is Result.Error).isTrue()
        assertThat((dataStoreRepository.getUser() is Result.Error)).isTrue()
    }

    @Test
    fun `sign in should successfully`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        val loggedUser = (userRepository.signIn(user) as Result.Success).data
        assertThat((userRepository.getUser() as Result.Success).data).isEqualTo(loggedUser)
        assertThat((dataStoreRepository.getUser() as Result.Success).data).isEqualTo(loggedUser)
    }

    @Test
    fun `sign in with non-exist user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "333333")
        assertThat(userRepository.signIn(user) is Result.Error).isTrue()
        assertThat(userRepository.getUser() is Result.Error).isTrue()
        assertThat((dataStoreRepository.getUser() is Result.Error)).isTrue()
    }

    @Test
    fun `log out then get user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        userRepository.signIn(user)
        userRepository.logOut()
        assertThat(userRepository.getUser() is Result.Error).isTrue()
        assertThat((dataStoreRepository.getUser() is Result.Error)).isTrue()
    }

    @Test
    fun `update user profile with null image uri should successfully`() = runTest(testDispatcher) {
        val user = User(id = "id_user1", username = "user1_updated", email = "user1_updated@gmail.com", password = "111111")
        val updatedUser = (userRepository.setUser(user, null) as Result.Success).data
        assertThat(updatedUser).isEqualTo(user)
        assertThat((userRepository.getUser() as Result.Success).data).isEqualTo(updatedUser)
        assertThat((dataStoreRepository.getUser() as Result.Success).data).isEqualTo(updatedUser)
        assertThat(userRemoteDataSource.users["id_user1"]).isEqualTo(updatedUser)
    }

    @Test
    fun `update user profile with valid image uri should successfully`() = runTest(testDispatcher) {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "path_to_file"
        mockkStatic(Uri::class)
        every { Uri.parse("content://path_to_file") } returns mockUri

        val user = User(id = "id_user1", username = "user1_updated", email = "user1_updated@gmail.com", password = "111111")
        val updatedUser = (userRepository.setUser(user, Uri.parse("content://path_to_file")) as Result.Success).data
        assertThat(updatedUser).isEqualTo(user.copy(imageUrl = "network_path_to_file"))
        assertThat((userRepository.getUser() as Result.Success).data).isEqualTo(updatedUser)
        assertThat((dataStoreRepository.getUser() as Result.Success).data).isEqualTo(updatedUser)
        assertThat(userRemoteDataSource.users["id_user1"]).isEqualTo(updatedUser)
    }
}