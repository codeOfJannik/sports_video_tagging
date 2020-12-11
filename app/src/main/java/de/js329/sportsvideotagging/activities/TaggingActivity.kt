package de.js329.sportsvideotagging.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.fragments.TaggingMatchBaseFragment
import de.js329.sportsvideotagging.fragments.TaggingMatchEventFinalizeFragment
import de.js329.sportsvideotagging.fragments.TaggingMatchEventPlayersFragment

class TaggingActivity : AppCompatActivity(), TaggingFragmentManager {

    private val homeTeamId by lazy { intent.getLongExtra("homeTeamId", -1) }
    private val awayTeamId by lazy { intent.getLongExtra("awayTeamId", -1) }
    private val matchTaggingController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        MatchTaggingController(db.matchDao(), db.eventDao(), db.eventJoinDao(), db.teamDao(), db.playerDao())
    }
    private val matchTaggingFragment by lazy {
        TaggingMatchBaseFragment.newInstance(matchTaggingController, homeTeamId, awayTeamId, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(
                            R.id.fragment_container_view,
                            matchTaggingFragment
                    )
                    .commit()

        }
        setContentView(R.layout.activity_tagging)
    }

    override fun switchToPlayerSelection() {
        val playerSelectionFragment = TaggingMatchEventPlayersFragment.newInstance(matchTaggingController, this)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, playerSelectionFragment)
                .commit()
    }

    override fun switchToFinalizeMatchInput() {
        val finalizeMatchEventFragment = TaggingMatchEventFinalizeFragment.newInstance(matchTaggingController, this)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_view, finalizeMatchEventFragment)
            .commit()
    }
}

interface TaggingFragmentManager {
    fun switchToPlayerSelection()
    fun switchToFinalizeMatchInput()
}