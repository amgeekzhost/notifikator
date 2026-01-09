package com.geekzforwarder.notifikator

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Filter
import android.widget.FilterResults
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.card.MaterialCardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: AppsAdapter
    private lateinit var searchView: SearchView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var fabSelectAll: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        toolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_toggle_mode -> {
                    val newMode = !AllowedAppsStore.isWhitelistMode(this)
                    AllowedAppsStore.setWhitelistMode(this, newMode)
                    val modeText = if (newMode) "Only selected apps" else "All apps"
                    Toast.makeText(this, "Mode: $modeText", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        searchView = findViewById(R.id.search_view)
        recycler = findViewById(R.id.recycler_apps)
        recycler.layoutManager = LinearLayoutManager(this)

        val apps = loadLaunchableApps()
        adapter = AppsAdapter(apps, packageManager, AllowedAppsStore.getAllowedPackages(this))
        recycler.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        fabSelectAll = findViewById(R.id.fab_select_all)
        fabSelectAll.setOnClickListener {
            val allPackages = adapter.currentDisplayedApps.map { it.packageName }
            val allowed = AllowedAppsStore.getAllowedPackages(this)
            if (allowed.containsAll(allPackages)) {
                AllowedAppsStore.clearAll(this)
                adapter.updateAllowed(emptySet())
                Toast.makeText(this, "Cleared selection", Toast.LENGTH_SHORT).show()
            } else {
                AllowedAppsStore.addAll(this, allPackages)
                adapter.updateAllowed(AllowedAppsStore.getAllowedPackages(this))
                Toast.makeText(this, "Selected all visible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadLaunchableApps(): List<ApplicationInfo> {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
    }

    class AppsAdapter(
        private val apps: List<ApplicationInfo>,
        private val pm: PackageManager,
        private var allowed: Set<String>
    ) : RecyclerView.Adapter<AppsAdapter.ViewHolder>(), android.widget.Filterable {

        var currentDisplayedApps: List<ApplicationInfo> = apps.toList()
            private set

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app_toggle, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = currentDisplayedApps[position]
            val label = pm.getApplicationLabel(app).toString()
            val icon = pm.getApplicationIcon(app)
            holder.icon.setImageDrawable(icon)
            holder.label.text = label
            holder.pkg.text = app.packageName
            val isChecked = allowed.contains(app.packageName)
            holder.toggle.isChecked = isChecked

            holder.toggle.setOnCheckedChangeListener { _, isCheckedNow ->
                val ctx = holder.itemView.context
                if (isCheckedNow) {
                    AllowedAppsStore.addPackage(ctx, app.packageName)
                } else {
                    AllowedAppsStore.removePackage(ctx, app.packageName)
                }
                allowed = AllowedAppsStore.getAllowedPackages(ctx)
            }

            holder.card.setOnClickListener {
                holder.toggle.isChecked = !holder.toggle.isChecked
            }
        }

        override fun getItemCount(): Int = currentDisplayedApps.size

        fun updateAllowed(newAllowed: Set<String>) {
            this.allowed = newAllowed
            notifyDataSetChanged()
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                    val result = if (query.isEmpty()) {
                        apps
                    } else {
                        apps.filter {
                            pm.getApplicationLabel(it).toString().lowercase().contains(query) ||
                                    it.packageName.contains(query)
                        }
                    }
                    val r = FilterResults()
                    r.values = result
                    return r
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    currentDisplayedApps = (results?.values as? List<ApplicationInfo>) ?: emptyList()
                    notifyDataSetChanged()
                }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.card)
            val icon: ImageView = view.findViewById(R.id.app_icon)
            val label: TextView = view.findViewById(R.id.app_label)
            val pkg: TextView = view.findViewById(R.id.app_package)
            val toggle: SwitchMaterial = view.findViewById(R.id.app_switch)
        }
    }
}
