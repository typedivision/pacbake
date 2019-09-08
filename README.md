![pacstage](pacstage.png)

creates minimal target (raspberry pi) images based on
[bitbake](https://www.yoctoproject.org/docs/current/bitbake-user-manual/bitbake-user-manual.html),
[crosstool-ng](https://crosstool-ng.github.io) and
[pacman](https://www.archlinux.org/pacman/).

Get the build environment with
```
ci/docker.run.sh
# . ci/setup.in
```
and then
```
# bitbake <recipe>  # build a package or image
# bitbake world     # buile everything
# bitbake -s        # get a list of recipes
```
