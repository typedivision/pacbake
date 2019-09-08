FROM typedivision/arch-micro:190801

RUN pacman -Sy --noconfirm --needed --quiet \
      arch-install-scripts python3 wget tar unzip git mercurial bubblewrap tmux vim \
 && pacman -Scc --noconfirm

CMD /bin/bash
