# Copyright (C) 2017- present Juergen Zimmermann, Hochschule Karlsruhe
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

# Aufruf:   kunde [cmd=start|stop|shutdown]

# "Param" muss in der 1. Zeile sein
Param (
    [string]$cmd = "start"
)

Set-StrictMode -Version Latest

# Titel setzen
$script = $myInvocation.MyCommand.Name
$host.ui.RawUI.WindowTitle = $script

function StartServer {
    ./gradlew
}

function StopServer {
    $schema = "https"
    # TLS 1.3 erfordert mind. OpenSSL 1.1.1
    # OpenSSH ist inkompatibel mit OpenSSL 1.1.x
    #    https://github.com/git-for-windows/git/issues/1691
    #    http://slproweb.com/products/Win32OpenSSL.html
    # SChannel ("WinSSL") kann noch nicht TLS 1.3
    #    https://curl.haxx.se/docs/ssl-compared.html
    #    https://en.wikipedia.org/wiki/Comparison_of_TLS_implementations
    #$tls = "--tlsv1.3"
    $tls = "--tlsv1.2"

    #$schema = "http"
    #$tls = ""

    #$http2 = "--http2"
    $http2 = ""

    $port = "8444"
    $username = "admin"
    $password = "p"

    $url = "${schema}://localhost:${port}/actuator/shutdown"
    $basicAuth = "${username}:${password}"

    # Alternative zur curl: HTTPie von https://httpie.org kein TLS 1.3
    C:\Zimmermann\Git\mingw64\bin\curl -v -H "Content-Type:application/json" `
        -d "{}" --basic -u $basicAuth $tls $http2 -k $url
}

switch ($cmd) {
    "start" { StartServer; break }
    "stop" { StopServer; break }
    "shutdown" { StopServer; break }
    default { write-host "$script [cmd=start|stop|shutdown]" }
}
