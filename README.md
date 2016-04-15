# Plumbot

A Clojure library to host slack bots.

## Usage

The Plumbot uses Stuart Sierra's [component library](https://github.com/stuartsierra/component).

To begin, call `com.plumbee.plumbot.system/plumbot-system` to obtain a system map.
Then call `com.stuartsierra.component/start` and `com.stuartsierra.component/stop` on this map to start and stop the system.

Bots are records of type `com.plumbee.plumbot.register/Bot` and can be installed by calling
 `com.plumbee.plumbot.register/register-bot!` on them.

See the [example namespace](doc/example.clj) for a concrete example or for a more in
depth example take a look at our [Build Bot](https://github.com/plumbee/buildbot).

### Configuration subsystem

The Plumbot provides a couple of configuration loading methods.

`load-data` reads [edn](https://github.com/edn-format/edn) (Clojure data) from the filesystem.
`persistent-atom` performs a similar action, but stores the result in an atom, and propagates changes to the atom back to the file system.

Hence, `load-data` is more suitable for static config, and `persistent-atom` is more suitable for bot state that you wish to survive a restart.
At Plumbee our bot is backed by [EBS](https://aws.amazon.com/ebs/) so that state survives when the stack is rebuilt.

If you don't know what config to provide, try starting the bot, and then see what configuration files are created.

Note that both methods only read once (at evaluation time) from the file system and that `persistent-atom` may overwrite manual changes to the file.
Thus it is best to stop the Plumbot while editing config files.


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