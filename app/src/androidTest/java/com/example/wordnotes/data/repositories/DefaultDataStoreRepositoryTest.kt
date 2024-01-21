package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DefaultDataStoreRepositoryTest {

    @Inject
    lateinit var dataStoreRepository: DefaultDataStoreRepository

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun cleanUp() = runTest {
        dataStoreRepository.clear()
    }

    @Test
    fun whenThereIsNoUserThenGetShouldReturnError() = runTest {
        val result = dataStoreRepository.getUser()
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun setNewUserShouldReturnSuccess() = runTest {
        val user = User(id = "1", username = "user1")
        val result = dataStoreRepository.setUser(user)
        assertThat((result as Result.Success).data).isEqualTo(user)
    }

    @Test
    fun setNewUserAndGetShouldReturnSuccess() = runTest {
        val user = User(id = "1", username = "user1")
        dataStoreRepository.setUser(user)
        val result = dataStoreRepository.getUser()
        assertThat((result as Result.Success).data).isEqualTo(user)
    }

    @Test
    fun setNewUserThenClearUserThenGetShouldReturnError() = runTest {
        val user = User(id = "1", username = "user1")
        dataStoreRepository.setUser(user)
        dataStoreRepository.clearUser()
        val result = dataStoreRepository.getUser()
        assertThat(result is Result.Error).isTrue()
    }
}