# PostgreSQL Vault Auth

![Build](https://github.com/davidsteinsland/intellij-vault/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16104-postgresql-vault-auth.svg)](https://plugins.jetbrains.com/plugin/16104-postgresql-vault-auth)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16104-postgresql-vault-auth.svg)](https://plugins.jetbrains.com/plugin/16104-postgresql-vault-auth)

<!-- Plugin description -->
Fetches credentials for a PostgreSQL database from
Vault.

This plugin assumes that `vault` is installed and available.
If you're not authenticated against Vault, the OIDC method
is selected (which will trigger your browser to open a tab with the signin process).
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Postgres Vault Auth"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/davidsteinsland/intellij-vault/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
  
### Postgres SVG logo Copyright

Copyright © 2021, Daniel Lundin

Permission to use, copy, modify, and distribute this software and its documentation for any purpose, without fee, and without a written agreement is hereby granted, provided that the above copyright notice and this paragraph and the following two paragraphs appear in all copies.

IN NO EVENT SHALL THE AUTHOR BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
THE AUTHOR SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE AUTHOR HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.