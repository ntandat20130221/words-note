package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.sharedtest.FakeDataStoreRepository
import com.example.wordnotes.sharedtest.FakeUserNetworkDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultUserRepositoryTest {
    private lateinit var userNetworkDataSource: FakeUserNetworkDataSource
    private lateinit var dataStoreRepository: FakeDataStoreRepository
    private lateinit var userRepository: DefaultUserRepository

    @Before
    fun setUp() {
        userNetworkDataSource = FakeUserNetworkDataSource()
        dataStoreRepository = FakeDataStoreRepository()
        userRepository = DefaultUserRepository(userNetworkDataSource, dataStoreRepository)
    }

    @Test
    fun `test sign up`() = runTest {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        val result = userRepository.signUp(user)
        assertThat(result is Result.Success).isTrue()

        val user2 = User(username = "user2", email = "user1@gmail.com", password = "123456")
        val result2 = userRepository.signUp(user2)
        assertThat(result2 is Result.Error).isTrue()
    }

    @Test
    fun `test sign in`() = runTest {
        val user = User(username = "user1", email = "user1@gmail.com", password = "123456")
        val result = userRepository.signIn(user)
        assertThat(result is Result.Error).isTrue()

        userRepository.signUp(user)
        val result2 = userRepository.signIn(user)
        assertThat(result2 is Result.Success).isTrue()
    }
}