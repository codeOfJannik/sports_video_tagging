package de.js329.sportsvideotagging.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.fragments.MatchTaggingBaseFragment
import kotlinx.coroutines.launch

class TaggingActivity : AppCompatActivity() {

    private val homeTeamId by lazy { intent.getLongExtra("homeTeamId") }
    private val awayTeamId by lazy { intent.getLongExtra("awayTeamId") }
    private val matchTaggingController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        MatchTaggingController(db.matchDao(), db.eventDao(), db.eventJoinDao(), db.teamDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.fragment_container_view,
                        MatchTaggingBaseFragment.newInstance(matchTaggingController, homeTeamId, awayTeamId)
                    )
                    .commit()
            }
        }

        setContentView(R.layout.activity_tagging)
    }
}