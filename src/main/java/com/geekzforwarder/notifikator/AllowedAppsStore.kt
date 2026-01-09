package com.geekzforwarder.notifikator

import android.content.Context

object AllowedAppsStore {
    private const val PREFS = "allowed_apps_prefs"
    private const val KEY_ALLOWED = "allowed_packages"
    private const val KEY_MODE_WHITELIST = "mode_whitelist" // true = only selected apps, false = all apps

    fun isWhitelistMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MODE_WHITELIST, true)
    }

    fun setWhitelistMode(context: Context, whitelist: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MODE_WHITELIST, whitelist).apply()
    }

    fun getAllowedPackages(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ALLOWED, emptySet()) ?: emptySet()
    }

    fun setAllowedPackages(context: Context, packages: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ALLOWED, packages).apply()
    }

    fun isAllowed(context: Context, packageName: String): Boolean {
        if (!isWhitelistMode(context)) return true
        val allowed = getAllowedPackages(context)
        return allowed.contains(packageName)
    }

    fun addPackage(context: Context, packageName: String) {
        val set = getAllowedPackages(context).toMutableSet()
        set.add(packageName)
        setAllowedPackages(context, set)
    }

    fun removePackage(context: Context, packageName: String) {
        val set = getAllowedPackages(context).toMutableSet()
        set.remove(packageName)
        setAllowedPackages(context, set)
    }

    fun clearAll(context: Context) {
        setAllowedPackages(context, emptySet())
    }

    fun addAll(context: Context, packages: Collection<String>) {
        val set = getAllowedPackages(context).toMutableSet()
        set.addAll(packages)
        setAllowedPackages(context, set)
    }
}
