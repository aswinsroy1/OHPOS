sed -i 's/title: String? = null,/title: String? = null,\n    subtitle: @Composable (() -> Unit)? = null,/g' app/src/main/java/com/example/ui/components/AppTopBar.kt
