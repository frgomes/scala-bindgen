#!/usr/bin/env bash

# Enable strict mode and fail the script on non-zero exit code,
# unresolved variable or pipe failure.
set -euo pipefail
IFS=$'\n\t'

if [ "$(uname)" == "Darwin" ]; then

    brew update
    brew install sbt
    brew install bdw-gc
    brew link bdw-gc
    brew install jq
    brew install re2
    brew install llvm@4
    export PATH="/usr/local/opt/llvm@4/bin:$PATH"

else
    # Install lsb-release, if needed
    [[ ! -e $(which lsb_release) ]] \
      && sudo apt-get -qq update \
        && sudo apt-get install -y -qq lsb-release

    # Install repositories
    case $(lsb_release -is) in
        "Ubuntu")
            [[ ! -f /etc/apt/sources.list.d/ubuntu-toolchain-r-ubuntu-test-artful.list ]] \
              && sudo add-apt-repository --yes ppa:ubuntu-toolchain-r/test \
                && sudo apt-get -qq update
            ;;
        "Debian")
            ;;
        *)
            echo ERROR: Distribution $(lsb_release -is) is not supported
            ;;
    esac

    # Install LLVM/Clang, Boehm GC, libunwind
    sudo apt-get install -y -qq clang++-3.8 libgc-dev libunwind8 libunwind8-dev zlib1g-dev

    # Install distribution dependent packages
    case $(lsb_release -is) in
        "Ubuntu")
            sudo apt-get update
            sudo apt-get install -y make
            export CXX=clang++-3.8
            # Install re2
            # Starting from Ubuntu 16.04 LTS, it'll be available as http://packages.ubuntu.com/xenial/libre2-dev
            git clone https://code.googlesource.com/re2
            pushd re2
            git checkout 2017-03-01
            make -j4 test
            sudo make install prefix=/usr
            make testinstall prefix=/usr
            popd
            ;;
        "Debian")
            sudo apt-get install -y -qq libre2-dev
            ;;
        *)
            ;;
    esac
fi
