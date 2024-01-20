package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.mocks.FakeDataStoreRepository
import com.example.wordnotes.mocks.FakeUserRemoteDataSource
import com.google.common.truth.Truth.assertThat
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
        val result = userRepository.signUp(user)
        assertThat(result is Result.Success).isTrue()
    }

    @Test
    fun `sign up with existing user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        val result = userRepository.signUp(user)
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun `sign in should successfully`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        val result = userRepository.signIn(user)
        assertThat(result is Result.Success).isTrue()
    }

    @Test
    fun `sign in with non-exist user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "333333")
        val result = userRepository.signIn(user)
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun `sign up then check current user profile`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "333333")
        val newUser = (userRepository.signUp(user) as Result.Success).data
        val currentUser = (userRepository.getUser() as Result.Success).data
        assertThat(currentUser).isEqualTo(newUser)
    }

    @Test
    fun `sign in then check current user profile`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        val loggedUser = (userRepository.signIn(user) as Result.Success).data
        val currentUser = (userRepository.getUser() as Result.Success).data
        assertThat(currentUser).isEqualTo(loggedUser)
    }

    @Test
    fun `log out then get user should return error`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "111111")
        userRepository.signIn(user)
        userRepository.logOut()
        assertThat(userRepository.getUser() is Result.Error).isTrue()
    }
}