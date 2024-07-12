# AdvancedHoppers by courtega

Simplify hopper configurations and save on materials with this small, performant utility plugin! Filter expressions
follow a simple syntax that takes less than three minutes to learn.

## What's different?

AdvancedHoppers deviates from HopperFilter in a few ways:

- Added chat integration, notifying you when you're editing a filter expression, and notifying you when an edit was
  cancelled.
- Items that aren't accepted by the filtered hopper will no longer clog it!
- Slight performance optimization (in my basic testing, RAM usage was reduced by 20%, but YMMV)
- Removed grouping separator and replaced it with an OR operator.
- Fixed enchantment/level delimitation issue by switching from `_` to a more intuitive `:`.
- Etc., etc., just check commits on GitHub

## How do I use it?

There are two primary ways of setting a filter:

1. Hold the item you want to filter for in your primary hand, then sneak and smack the hopper (`Shift + Left Click`).
2. With nothing in your hand, sneak and smack the hopper (`Shift + Left Click`), then type in chat the filter expression
   you want to set.

## Recent changelog

### 1.0.0-alpha.2

- Added a `config.toml`, allowing administrators to change settings and functionality with ease.
- Added a `locale.toml`! This lets servers targeting non-English-speaking player bases to easily translate dialogue into
  their native tongue!
- Fixed a couple small bugs.

_Notes: I've decided to go with TOML as the configuration format of choice because I personally find YAML disgusting. I
think that TOML is a fair balance between ease-of-use and structure._

**Important:** As you can see, this is an "alpha" release. Changes will continue to be made to improve performance and
the overall player experience.

## Contributors

If you'd like to contribute, feel free to make a pull request, and you will be credited accordingly! This plugin is
mainly a side project for me to improve my Java skills, but I will try to update things on a semi-regular basis.

## Current roadmap (subject to change)

- Implement better checking for invalid input
- Optimize and refactor the expression parser
- Improve performance
- Add support for comparative statements
- Add backwards compatibility for older Minecraft versions
- Fun statistics features (a hard "maybe")

## Blah, blah, legalese

I don't care if you use this plugin or its code to go make your own thing, but you must give me credit, and your project
must be licensed under GPLv3, because that's how GPLv3 works.

I see you copying my changes without crediting me; do not do it again. Thanks

## Support

If you want to show your appreciation for the weird little plugins I work on, the best thing you can do is share them
with your friends and help to improve them by submitting issues to the issue tracker on GitHub.

## Credits

AdvancedHoppers is a rewrite of jwkerr's HopperFilter plugin. That means this codebase is licensed under GPLv3. That fun
little document resides in `/LICENSE`.
