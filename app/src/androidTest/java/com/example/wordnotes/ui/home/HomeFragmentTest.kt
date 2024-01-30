package com.example.wordnotes.ui.home

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperLogged
import com.example.wordnotes.testutils.atPosition
import com.example.wordnotes.testutils.getString
import com.example.wordnotes.testutils.hasItemCount
import com.example.wordnotes.testutils.isFabExtended
import com.example.wordnotes.testutils.withBackgroundColor
import com.example.wordnotes.testutils.withCheckedItem
import com.example.wordnotes.testutils.withNavController
import com.example.wordnotes.ui.MainActivity
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Assume.assumeThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @BindValue
    @JvmField
    val loggedFirebaseAuth: FirebaseAuthWrapper = TestFirebaseAuthWrapperLogged()

    @Inject
    lateinit var wordRepository: WordRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun checkIfThisFragmentIsHomeFragment() {
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
    }

    @Test
    fun clickFabAddWordShouldNavigateToAddEditWordFragment() {
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.add_edit_word_fragment) }

        // And back should return HomeFragment.
        closeSoftKeyboard()
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
    }

    @Test
    fun toolbarAndBottomBarShouldDisplayCorrectlyOnScrollRecyclerView() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val height = uiDevice.displayHeight
        val width = uiDevice.displayWidth

        // Swipe up should hide Toolbar and BottomNav, FAB is collapsed.
        uiDevice.swipe(width / 2, height / 2, width / 2, 300, 50)
        onView(withId(R.id.toolbar)).check(matches(not(isCompletelyDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isCompletelyDisplayed())))
        onView(withId(R.id.fab_add_word)).check(matches(isFabExtended(false)))

        // Swipe down should show Toolbar and BottomNav, FAB is extended..
        uiDevice.swipe(width / 2, 300, width / 2, height / 2, 50)
        onView(withId(R.id.toolbar)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(isFabExtended(true)))
    }

    @Test
    fun clickAnyItemShouldOpenWordDetailFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.word_detail_fragment) }
    }

    @Test
    fun startActionModeThenTitleShouldDisplay1AndItemBackgroundShouldChange() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, longClick()))
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("1")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun stopActionModeThenItemBackgroundShouldRestoreDefault() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(3, click()))
        onView(withId(androidx.appcompat.R.id.action_mode_close_button)).perform(click())
        onView(withId(R.id.words_recycler_view))
            .check(matches(atPosition(2, withBackgroundColor(com.google.android.material.R.attr.colorSurface))))
        onView(withId(R.id.words_recycler_view))
            .check(matches(atPosition(3, withBackgroundColor(com.google.android.material.R.attr.colorSurface))))
    }

    @Test
    fun fabAndBottomNavShouldDisplayCorrectlyWhenStartAndStopActionMode() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start ActionMode, FAB and BottomNav should invisible.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Rotate to left then bottom nav and fab should invisible.
        uiDevice.setOrientationLeft()
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Stop ActionMode, FAB and BottomNav should visible.
        onView(withId(androidx.appcompat.R.id.action_mode_close_button)).perform(click())
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))

        // Rotate to natural orientation then bottom nav and fab should visible.
        uiDevice.setOrientationNatural()
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun clickLastItemShouldStopActionMode() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun actionModeStateShouldUnchangedAfterRotatingTheScreen() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start ActionMode then click some items.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))

        // Rotate to left then check ActionMode state.
        uiDevice.setOrientationLeft()
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))

        // Click another item.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(8, click()))

        // Rotate to natural orientation then check ActionMode state.
        uiDevice.setOrientationNatural()
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(1))
            .check(matches(atPosition(1, withBackgroundColor(com.google.android.material.R.attr.colorSurface))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(2))
            .check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(8))
            .check(matches(atPosition(8, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun editMenuShouldHideWhenThereIsMoreThanOneItemIsSelected() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(R.id.menu_edit)).check(doesNotExist())
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
    }

    @Test
    fun clickEditMenuInActionModeShouldNavigateToAddEditWordFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word2")), longClick()))
        onView(withId(R.id.menu_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.add_edit_word_fragment) }
        onView(withId(R.id.input_word)).check(matches(withText("word2")))

        // Press back should return HomeFragment and ActionMode is gone.
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun clickDeleteMenuInActionModeShouldShowSnakeBarAndStopActionMode() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word2")), click()))
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(getString(R.string.deleted_template, 2))))
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun deleteItemsInActionModeShouldDeleteItemsInRecyclerViewButRepositoryItemsHaveNotBeenDeletedYet() = runTest {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word2")), click()))
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(9))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(11)
    }

    @Test
    fun deleteItemsThenWaitForUndoSnackBarDismissedThenRepositoryItemsShouldActuallyDeleted() = runTest {
        // Delete some items.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word2")), click()))
        onView(withId(R.id.menu_delete)).perform(click())

        // Wait for SnackBar dismissed.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.wait(Until.gone(By.hasDescendant(By.res("com.example.wordnotes:id/snackbar_text"))), 3000)
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(9))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(9)
    }

    @Test
    fun deleteItemsThenUndoThenRecyclerViewItemsShouldReturnAndRepositoryItemsShouldNotDeleted() = runTest {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word2")), click()))
        onView(withId(R.id.menu_delete)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click())
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(11))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(11)
    }

    @Test
    fun clickRemindMenuInActionModeThenRecyclerViewItemsAndRepositoryShouldUpdateCorrectly() = runTest {
        assumeThat((wordRepository.getWords() as Result.Success).data[0].isRemind, `is`(true))
        assumeThat((wordRepository.getWords() as Result.Success).data[2].isRemind, `is`(false))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))
        onView(withId(R.id.menu_remind)).perform(click())

        // ActionMode should stop.
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, hasDescendant(withId(R.id.image_remind)))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, hasDescendant(withId(R.id.image_remind)))))
        assertThat((wordRepository.getWords() as Result.Success).data[0].isRemind).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data[2].isRemind).isTrue()
    }

    @Test
    fun clickSelectAllMenuInActionModeThenRecyclerViewShouldUpdateCorrectlyAndUiStateShouldUnChangedAfterRotatingTheScreen() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        openContextualActionModeOverflowMenu()
        onView(withText(R.string.select_all)).perform(click())
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("11")))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(0))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(1))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(2))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(3))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(3, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(4))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(4, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(5))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(5, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(6))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(6, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(7))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(7, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(8))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(8, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(9))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(9, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(10))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(10, withBackgroundColor(R.attr.color_selected_item_background))))

        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()

        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("11")))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(0))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(1))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(2))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(3))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(3, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(4))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(4, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(5))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(5, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(6))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(6, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(7))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(7, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(8))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(8, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(9))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(9, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).perform(scrollToPosition<WordsViewHolder>(10))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(10, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun openSearchThenCheckIfSearchViewIsVisibleBeforeAndAfterConfigurationChanged() {
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(not(isDisplayed())))
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.button_back)).perform(click())
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(not(isDisplayed())))
    }

    @Test
    fun searchItemsThenRecyclerViewShouldUpdateCorrectly() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("wo"))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(11))
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word12"))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(0))
    }

    @Test
    fun searchItemsShouldUnchangedAfterRotateTheScreen() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
    }

    @Test
    fun openWordDetailFragmentInSearchThenBottomNavShouldRemainHide() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))

        // Check if WordDetailFragment is opened.
        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.word_detail_fragment) }

        // BottomNav should hide.
        onView(withId(R.id.bottom_nav)).check(doesNotExist())

        // BottomNav should hide after configuration changed.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        uiDevice.setOrientationNatural()
        onView(withId(R.id.bottom_nav)).check(doesNotExist())
    }

    @Test
    fun navigateToAddEditWordFragmentWithWordDetailFragmentInSearchThenStateShouldUnchangedAfterGoingBack() {
        // Search some items then navigate to AddEditWordFragment.
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.action_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Rotate the screen.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()

        // Press back then check ui state.
        pressBack()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word1")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // State should unchanged after rotate the screen.
        uiDevice.setOrientationNatural()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word1")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Press back should close search.
        pressBack()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToAddEditWordFragmentWithActionModeInSearchThenStateShouldUnchangedAfterGoingBack() {
        // Search some items then navigate to AddEditWordFragment.
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.menu_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Rotate the screen.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()

        // Press back then check ui state.
        pressBack()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word1")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // State should unchanged after rotate the screen.
        uiDevice.setOrientationNatural()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word1")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        // Press back should close search.
        pressBack()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun openSearchWhenBottomIsSlideDownThenPressBackThenBottomNavShouldStillSlideDown() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val height = uiDevice.displayHeight
        val width = uiDevice.displayWidth

        // Swipe up should hide Toolbar and BottomNav, FAB is collapsed.
        uiDevice.swipe(width / 2, height / 2, width / 2, 300, 50)
        uiDevice.swipe(width / 2, 500, width / 2, height / 2, 200)

        onView(withId(R.id.bottom_nav)).check(matches(not(isCompletelyDisplayed())))
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        closeSoftKeyboard()
        pressBack()
        onView(withId(R.id.bottom_nav)).check(matches(not(isCompletelyDisplayed())))
    }

    @Test
    fun startActionModeInSearchShouldSuccessfully() {
        // Start searching.
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))

        // Start ActionMode
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word10")), click()))
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(isDisplayed()))
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("2")))

        // Rotate the screen then ActionMode should remain started.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(isDisplayed()))
        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("2")))

        // Press back should return search.
        pressBack()
        onView(withId(com.google.android.material.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word1")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
    }

    @Test
    fun startActionModeInSearchShouldHideSoftKeyboard() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        assertThat(isKeyboardOpened()).isFalse()
    }

    @Test
    fun openSearchThenClickDeleteMenuInActionModeShouldShowSnakeBarAndStopActionMode() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word10")), click()))
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(getString(R.string.deleted_template, 2))))
        onView(withId(androidx.appcompat.R.id.action_mode_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    fun openSearchThenDeleteItemsInActionModeShouldUpdateItemsInRecyclerViewButRepositoryItemsHaveNotBeenDeletedYet() = runTest {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word10")), click()))
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(1))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(11)
    }

    @Test
    fun openSearchThenDeleteItemsThenWaitForUndoSnackBarDismissedThenRepositoryItemsShouldActuallyDeleted() = runTest {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))

        // Delete some items.
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word10")), click()))
        onView(withId(R.id.menu_delete)).perform(click())

        // Wait for SnackBar dismissed.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.wait(Until.gone(By.hasDescendant(By.res("com.example.wordnotes:id/snackbar_text"))), 3000)
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(1))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(9)
    }

    @Test
    fun openSearchThenDeleteItemsThenUndoThenRecyclerViewItemsShouldReturnAndRepositoryItemsShouldNotDeleted() = runTest {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))

        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word10")), click()))
        onView(withId(R.id.menu_delete)).perform(click())

        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click())
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(11)
    }

    @Test
    fun openSearchThenSelectAllItemsThenRemindThenRecyclerViewAndRepositoryShouldUpdateCorrectly() = runTest {
        assumeThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word1" }!!.isRemind, `is`(true))
        assumeThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word10" }!!.isRemind, `is`(true))
        assumeThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word11" }!!.isRemind, `is`(false))

        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(replaceText("word1"))

        onView(withId(R.id.search_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), longClick()))
        openContextualActionModeOverflowMenu()
        onView(withText(R.string.select_all)).perform(click())

        onView(withId(androidx.appcompat.R.id.action_bar_title)).check(matches(withText("3")))
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))

        onView(withId(R.id.menu_remind)).perform(click())
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(0, hasDescendant(withId(R.id.image_remind)))))
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(1, hasDescendant(withId(R.id.image_remind)))))
        onView(withId(R.id.search_recycler_view)).check(matches(atPosition(2, hasDescendant(withId(R.id.image_remind)))))
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word1" }!!.isRemind).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word10" }!!.isRemind).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.word == "word11" }!!.isRemind).isTrue()
    }

    private fun isKeyboardOpened(): Boolean = UiDevice
        .getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("dumpsys input_method | grep mInputShown").contains("mInputShown=true")
}