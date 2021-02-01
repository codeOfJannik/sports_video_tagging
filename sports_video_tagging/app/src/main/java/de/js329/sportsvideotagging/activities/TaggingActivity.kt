package de.js329.sportsvideotagging.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.fragments.TaggingMatchBaseFragment
import de.js329.sportsvideotagging.fragments.TaggingMatchEventFinalizeFragment
import de.js329.sportsvideotagging.fragments.TaggingMatchEventPlayersFragment
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

class TaggingActivity : AppCompatActivity(), TaggingFragmentManager {

    private val homeTeamId by lazy { intent.getLongExtra("homeTeamId", -1) }
    private val awayTeamId by lazy { intent.getLongExtra("awayTeamId", -1) }
    private val matchDateTime by lazy {
        intent.getLongExtra("matchDate", -1)
    }
    private val matchTaggingController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        MatchTaggingController(db.matchDao(), db.eventDao(), db.eventJoinDao(), db.teamDao(), db.playerDao())
    }
    private val matchTaggingFragment by lazy {
        TaggingMatchBaseFragment.newInstance(matchTaggingController, homeTeamId, awayTeamId, matchDateTime, this)
    }
    private var allowBack = true

    override fun onBackPressed() {
        if (allowBack) {
            super.onBackPressed()
        } else {
            supportFragmentManager.findFragmentByTag("BASE")?.isVisible?.let {
                if (it) {
                    matchTaggingFragment.onMatchTaggingStopClicked()
                    return
                }
            }
            supportFragmentManager.findFragmentByTag("PLAYER")?.isVisible?.let {
                if (it) {
                    lifecycleScope.launch { matchTaggingController.cancelEventCreation() }
                    switchToBaseFragment()
                    return
                }
            }
            supportFragmentManager.findFragmentByTag("FINALIZE")?.isVisible?.let {
                if (it) {
                    lifecycleScope.launch {
                        matchTaggingController.deleteEventPlayersForLatestMatchEvent()
                        matchTaggingController.deleteFollowingEventIfPresent()
                    }
                    switchToPlayerSelection()
                    return
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(
                            R.id.fragment_container_view,
                            matchTaggingFragment,
                            "BASE"
                    )
                    .commit()
        }
        setContentView(R.layout.activity_tagging)
    }

    override fun switchToPlayerSelection() {
        val playerSelectionFragment = TaggingMatchEventPlayersFragment.newInstance(matchTaggingController, this)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, playerSelectionFragment, "PLAYER")
                .commit()
    }

    override fun switchToFinalizeMatchInput() {
        val finalizeMatchEventFragment = TaggingMatchEventFinalizeFragment.newInstance(matchTaggingController, this)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, finalizeMatchEventFragment, "FINALIZE")
            .commit()
    }

    override fun switchToBaseFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, matchTaggingFragment)
                .commit()
    }

    override fun onTaggingFinishedCloseFragments() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun matchTaggingStarted() {
        allowBack = false
    }
}

interface TaggingFragmentManager {
    fun matchTaggingStarted()
    fun switchToPlayerSelection()
    fun switchToFinalizeMatchInput()
    fun switchToBaseFragment()
    fun onTaggingFinishedCloseFragments()
}