# AdvancedHoppers by courtega

Simplify hopper configurations and save on materials with this small, performant utility plugin. Filter expressions
follow a simple syntax that takes less than a minute to master.

## Contributors

If you'd like to contribute, feel free to make a pull request, and you will be credited accordingly. I'm probably not
going to be spending too much time on this. To be honest with you, I don't even use hoppers, I just hadn't touched Java
in five years and wanted a little project to do.

## Simple ASL Tutorial

1. Sneak and left-click a hopper with an item in your hand to filter for just that item.
2. Sneak and left-click a hopper with nothing in your hand to write a custom filter expression.
    - Do `/advancedhoppers help` for a list of filtering keywords (aka "operators") and their respective meanings.
3. You are done.

## Planned features and changes

- Deference of constants to a YAML file for easy modification by administrators.
- More performance tweaking and optimizations.
- Refactoring, refactoring, refactoring.
- Port to Kotlin because Java sucks

## Blah, blah, legalese

I don't care if you use this plugin or its code to go make your own thing, but you must give me credit, and your project
must be licensed under GPLv3.

### Index of changes from HopperFilter

- Added chat integration
    - Notified in chat when editing a filter expression
    - Notified in chat when a filter expression change is cancelled
- Excluded items residing in the first slot of a source container will no longer "clog up" your filtered hopper.
- Some performance optimizations (maybe ~20% less memory consumption? YMMV)
- Eradication of magic strings to make changes easier in the future (for example, if you want the `or` operator to be
  represented by `🤷‍♂️` or something stupid like that)
- A bunch of other shit just look at the commits

## Credits

AdvancedHoppers is a rewrite of jwkerr's HopperFilter plugin. That means this codebase is licensed under GPLv3. That fun
little document resides in `/LICENSE`.
