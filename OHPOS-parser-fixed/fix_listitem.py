with open("app/src/main/java/com/example/ui/components/ListItem.kt", "r") as f:
    content = f.read()

bad_str = """            .padding(16.dp)
    ).alpha(if (isPendingDeletion) 0.65f else 1f)
    ) {"""

good_str = """            .padding(16.dp)
            .alpha(if (isPendingDeletion) 0.65f else 1f)
    ) {"""

content = content.replace(bad_str, good_str)
with open("app/src/main/java/com/example/ui/components/ListItem.kt", "w") as f:
    f.write(content)
