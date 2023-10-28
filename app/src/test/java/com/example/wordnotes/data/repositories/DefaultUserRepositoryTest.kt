package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.sharedtest.FakeDataStoreRepository
import com.example.wordnotes.sharedtest.FakeUserNetworkDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultUserRepositoryTest {
    private lateinit var userNetworkDataSource: FakeUserNetworkDataSource
    private lateinit var dataStoreRepository: FakeDataStoreRepository
    private lateinit var userRepository: DefaultUserRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        userNetworkDataSource = FakeUserNetworkDataSource()
        dataStoreRepository = FakeDataStoreRepository()
        userRepository = DefaultUserRepository(userNetworkDataSource, dataStoreRepository, testDispatcher)
    }

    @Test
    fun `test sign up`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "123456")
        val result = userRepository.signUp(user)
        assertThat(result is Result.Success).isTrue()
    }

    @Test
    fun `sign up with existing user then return error`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        val result = userRepository.signUp(user)
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun `test sign in`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        val result = userRepository.signIn(user)
        assertThat(result is Result.Success).isTrue()
    }

    @Test
    fun `sign in with non-existing user then return error`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "123456")
        val result = userRepository.signIn(user)
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun `sign up then check current user`() = runTest(testDispatcher) {
        val user = User(username = "user3", email = "user3@gmail.com", password = "123456")
        val newUser = (userRepository.signUp(user) as Result.Success).data

        val currentUser = (userRepository.getUser() as Result.Success).data
        assertThat(currentUser).isEqualTo(newUser)
    }

    @Test
    fun `sign in then check current user`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        val signedInUser = (userRepository.signIn(user) as Result.Success).data

        val currentUser = (userRepository.getUser() as Result.Success).data
        assertThat(currentUser).isEqualTo(signedInUser)
    }

    @Test
    fun `log out then get user should return empty user`() = runTest(testDispatcher) {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        userRepository.signIn(user)
        userRepository.logOut()
        val currentUser = (userRepository.getUser() as Result.Success).data
        assertThat(currentUser).isEqualTo(
            User(id = "", username = "", email = "", password = "", phone = "", gender = "", dob = "")
        )
    }
}