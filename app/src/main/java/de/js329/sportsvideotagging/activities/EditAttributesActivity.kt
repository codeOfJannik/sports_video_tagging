package de.js329.sportsvideotagging.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.EventAttribute
import kotlinx.coroutines.launch

class EditAttributesActivity : AppCompatActivity() {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this)
        ConfigurationController(db.eventDao(), db.playerDao(), db. teamDao())
    }

    private var eventAttributes: MutableList<EventAttribute> = ArrayList()
    private val attributeAdapter = AttributesAdapter(this, eventAttributes)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.editAttributesHeader_txt)
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = attributeAdapter
        findViewById<FloatingActionButton>(R.id.addFAB).setOnClickListener {
            onAddAttributeClicked()
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            onLongItemClick(position)
        }
    }

    override fun onStart() {
        super.onStart()
        queryEventAttributes()
    }

    private fun onAddAttributeClicked() {
        var attributeName: String
        val builder = AlertDialog.Builder(this)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setTitle(R.string.addAttributeTitle_txt)
        builder.setMessage(R.string.addAttributeMessage_txt)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            attributeName = input.text.toString()
            if (attributeName.trim() != "") {
                lifecycleScope.launch {
                    val newAttribute = configurationController.addEventAttribute(attributeName)
                    newAttribute?.let {
                        eventAttributes.add(it)
                        updateList()
                    } ?: kotlin.run {
                        Toast.makeText(this@EditAttributesActivity, R.string.attributeAlreadyExists, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                dialog.cancel()
                Toast.makeText(this, R.string.noAttributeName, Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun onLongItemClick(position: Int): Boolean {
        val attribute = attributeAdapter.getItem(position)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.deleteAttributeTitle_txt)
        builder.setMessage(getString(R.string.deleteAttributeMessage_txt, attribute.attribute_name))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            lifecycleScope.launch {configurationController.deleteEventAttribute(attribute)}
            eventAttributes.remove(attribute)
            updateList()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        return true
    }

    private fun queryEventAttributes() {
        lifecycleScope.launch {
            eventAttributes = configurationController.getAllEventAttributes().toMutableList()
            updateList()
        }
    }

    private fun updateList() {
        attributeAdapter.attributes = eventAttributes
        attributeAdapter.notifyDataSetChanged()
    }

}

class AttributesAdapter(private val context: Context, var attributes: List<EventAttribute>): BaseAdapter() {
    override fun getCount(): Int {
        return attributes.size
    }

    override fun getItem(position: Int): EventAttribute {
        return attributes[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).attributeId ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        itemView.findViewById<TextView>(android.R.id.text1).text = getItem(position).attribute_name
        return itemView
    }

}