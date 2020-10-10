Stately
=======

**Stately** is an unofficial [NationStates](http://www.nationstates.net/) app for Android.

Get Stately on: [Google Play](https://play.google.com/store/apps/details?id=com.lloydtorres.stately) | [Amazon Appstore](http://www.amazon.com/gp/product/B01E4R7T1C/ref=mas_pm_stately_for_nationstates)

### Features

* Get your nation's stats, policies, rankings and happenings.
* Respond to issues encountered by your nation.
* Login and switch between different nations.
* Browse through and move to different regional communities.
* Chat with other players and more in RMBs.
* Observe and vote on current World Assembly resolutions.
* Endorse nations and participate in R/D gameplay.
* Read, manage, write and reply to telegrams.
* Discover trends in national, regional and world census statistics.
* Compile all your happenings together with the Activity Feed.
* Get notified and stay up to date on new issues, telegrams and other events.
* Make it your own: choose between five different themes and more.

### Revision History

Detailed release notes and APKs can be found in the [releases page](https://github.com/lloydtorres/stately/releases).

* **1.10.3** - Bug fixes and improvements.
* **1.10.2** - Support new census scales, alphabetical sort, longer trend graphs, visual polishes.
* **1.10.1** - Bug fixes and improvements.
* **1.10.0** - New logo, nation policies support.
* **1.9.3** - Z-Day improvements.
* **1.9.2** - Edit RMB posts, regional voting data in resolutions, Android Oreo support, bug fixes and improvements.
* **1.9.1** - Bug fix for aggressive notification service for Lollipop and above.
* **1.9.0** - New logo, issue decision API, Android O prep, bug fixes and improvements.
* **1.8.3** - Bug fixes and improvements.
* **1.8.2** - Issue images, WA badges, app licenses, bug fixes and improvements.
* **1.8.1** - Bug fixes and improvements.
* **1.8.0** - RMB suppressions, WA resolution vote breakdown, visual polishes, bug fixes and other improvements.
* **1.7.6** - Bug fixes and improvements.
* **1.7.5** - Bug fixes and improvements.
* **1.7.4** - Hotfix to deal with illegal state fragment transaction crashes.
* **1.7.3** - Hotfix for Z-Day.
* **1.7.2** - Z-Day support, issues API, bug fixes and other improvements.
* **1.7.1** - Restore text selection, bug fixes and other improvements.
* **1.7.0** - Notifications, themes, World, census rankings, open old resolutions, improved telegrams, region polls, dossier.
* **1.6.3** - Enable move to SD card, bug fixes and improvements.
* **1.6.2** - Bug fixes and improvements.
* **1.6.1** - Bug fix for rare EditText crash.
* **1.6.0** - API-based login, drawer unread count, telegram organization, reporting, swipe to close screens.
* **1.5.5** - Remove Guava, confirm app exit, UI changes, bug fixes and improvements.
* **1.5.4** - Bug fixes and improvements.
* **1.5.3** - Bug fixes.
* **1.5.2** - Hotfix for spoiler links.
* **1.5.1** - Better spoiler support, bug fixes.
* **1.5.0** - RMB improvements, census rankings/trends, vectorized icons, bug fixes and improvements.
* **1.4.2** - Additional safety checks, bug fixes and improvements.
* **1.4.1** - Bug fixes and improvements.
* **1.4.0** - Telegram support, increased network timeout.
* **1.3.4** - Bug fixes and improvements.
* **1.3.3** - Improved issue results screen.
* **1.3.2** - Bug fix for possible missing HTML element in issue results.
* **1.3.1** - Support for instant issues, bug fix for issue preventing login.
* **1.3.0** - Endorsements, move regions, RMB quote/delete, User-Agent, removed ads.
* **1.2.3** - Bug fixes and improvements.
* **1.2.2** - Fix bug where issue option header may be different from index.
* **1.2.1** - Fix bugs with detached fragment and missing HtmlTextViews.
* **1.2.0** - Activity feed, improved BBCode parsing, Material dialogs, Flurry.
* **1.1.0** - Multi-nation login, RMB posting, WA voting, rate limiter.
* **1.0.1** - Fix bug with malformed vectors.
* **1.0.0** - Initial release.

### Translations

English strings in the Stately app can be found in three files:

* [`res/values/strings.xml`](https://github.com/lloydtorres/stately/blob/master/Stately/app/src/main/res/values/strings.xml)
* [`res/values/plurals.xml`](https://github.com/lloydtorres/stately/blob/master/Stately/app/src/main/res/values/plurals.xml)
* [`res/values/arrays.xml`](https://github.com/lloydtorres/stately/blob/master/Stately/app/src/main/res/values/arrays.xml) (lines 4-51 only; rest are from NationStates or licensing)

To translate Stately into another language, fork this repository and create a copy of these files in a new directory called `res/values-[xx]/`, where `[xx]` represents the target language's [ISO 639-1 code](https://www.loc.gov/standards/iso639-2/php/code_list.php) (e.g. `fr` for French and `ja` for Japanese).

For languages with special pluralization rules, refer to the [official Android guide on pluralization](https://developer.android.com/guide/topics/resources/string-resource.html#Plurals) for information on how to format `res/values/plurals.xml`.

Commit to your fork and submit a pull request once your translation is ready. I'll reach out and work with you to properly integrate your translation into Stately.

Note that translations only affect in-app strings. Strings from the NationStates gameside (such as issues) will remain in English.

### Libraries

Stately uses the following open-source libraries:

* [Evo Inflector](https://github.com/atteo/evo-inflector)
* [HtmlTextView](https://github.com/SufficientlySecure/html-textview)
* [jsoup](http://jsoup.org/)
* [Material Components for Android](https://github.com/material-components/material-components-android)
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* [PagerSlidingTabStrip](https://github.com/jpardogo/PagerSlidingTabStrip)
* [Picasso](https://github.com/square/picasso)
* [Pulsator4Droid](https://github.com/booncol/Pulsator4Droid)
* [Simple XML](http://simple.sourceforge.net/)
* [Slidr](https://github.com/r0adkll/Slidr)
* [Sugar ORM](https://github.com/satyan/sugar)
* [SwipyRefreshLayout](https://github.com/omadahealth/SwipyRefreshLayout)
* [Volley](https://github.com/google/volley)

### Creative Commons

Stately uses Creative Commons-licensed content from the following sources:

* [Medical Icons Pack (CC BY 3.0)](http://www.flaticon.com/packs/medical-icons)

### License

```
Copyright 2016-2020 Lloyd Torres

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
