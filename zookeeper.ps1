# Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Param muss die erste Anweisung sein
Param (
    [string]$cmd = "start"
)

Set-StrictMode -Version Latest

# Titel setzen
$script = $myInvocation.MyCommand.Name
$host.ui.RawUI.WindowTitle = $script

$kafkaDir = $env:KAFKA_DIR

function StartServer {
    zookeeper-server-start $kafkaDir\config\zookeeper.properties
}

function StopServer {
    zookeeper-server-stop
    CleanKafka
}

function CleanKafka {
    $cwd = (Get-Location).path
    write-host $cwd

    cd $env:GIT_HOME/usr/bin
    .\rm -r $kafkaDir\logs
    .\rm -r $kafkaDir\zookeeper
    mkdir $kafkaDir\logs
    mkdir $kafkaDir\zookeeper

    cd $cwd
}

function Shell {
    write-host ""
    write-host "!!! Voraussetzung: Der Zookeeper-Server muss gestartet sein !!!"
    write-host "Interaktive Shell fuer Zookeeper fuer Zookeeper-Kommandos, z.B. 'ls /'"
    write-host "Hilfe durch das Kommando 'help'"
    write-host "Beenden durch das Kommando 'quit'"
    write-host ""
    zookeeper-shell localhost:2181
}

switch ($cmd) {
    "start" { StartServer; break }
    "stop" { StopServer; break }
    "shutdown" { StopServer; break }
    "shell" { Shell; break }
    default { write-host "$script [cmd=start|stop|shutdown|shell]" }
}
