# Plumbot

A Clojure library to host slack bots.

## Usage

The Plumbot uses Stuart Sierra's [component library](https://github.com/stuartsierra/component).

To begin, call `com.plumbee.plumbot.system/plumbot-system` to obtain a system map.
Then call `com.stuartsierra.component/start` and `com.stuartsierra.component/stop` on this map to start and stop the system.

Bots are records of type `com.plumbee.plumbot.register/Bot` and can be installed by calling
 `com.plumbee.plumbot.register/register-bot!` on them.

See the [example namespace](doc/example.clj) for a concrete example.


## License (See LICENSE file for full license)

Copyright © 2016 Plumbee Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.