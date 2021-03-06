# Matrix Studio

[![Build Status](https://travis-ci.org/pascal-ballet/matrixstudio.svg?branch=master)](https://travis-ci.org/pascal-ballet/matrixstudio)
[![Join the chat at https://gitter.im/jeancharles-roger/matrixstudio](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/jeancharles-roger/matrixstudio?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Matrix Studio is an Integrated Development Environment to develop OpenCL software. 
It helps ou create multicore software without coding the host (100% OpenCL).

[Web Site](http://virtulab.univ-brest.fr/?page_id=23)

Downloads for Windows 64:
- [3D Version with integrated java 64](https://www.amazon.fr/clouddrive/share/nQc9NbHpKyIYhpn4fu60rP30G5Q9hybHcJxZIu7rDcT)
- [2D Version](https://www.amazon.fr/clouddrive/share/a4b9R3jN1ezkr0DwqRuteXjm4B7Wmn4eg44p07BalgW)
- [MatrixStudio.bat](https://www.amazon.fr/clouddrive/share/e9EccQx5WuLkYFjKXZXolQOJgawnk5kmAJzsOIz3FZY)

Download daily builds (3D):
- [Macos version](https://bintray.com/jeancharles-roger/generic/download_file?file_path=matrixstudio%2Fdaily%2FMatrixStudio-daily-mac64-1.0.0.tar.gz)
- [Windows 64 version](https://bintray.com/jeancharles-roger/generic/download_file?file_path=matrixstudio%2Fdaily%2FMatrixStudio-daily-windows64-1.0.0.zip)
- [Windows 32 version](https://bintray.com/jeancharles-roger/generic/download_file?file_path=matrixstudio%2Fdaily%2FMatrixStudio-daily-windows32-1.0.0.zip)
- [Linux version](https://bintray.com/jeancharles-roger/generic/download_file?file_path=matrixstudio%2Fdaily%2FMatrixStudio-daily-linux64-1.0.0.tar.gz)

## Getting started

If you want to use Matrix Studio, just download the distribution for your platform on using on of the links above.

If you want to tinker with the code: fork or clone the repository:
```sh
git clone https://github.com/pascal-ballet/matrixstudio.git
```

Then you can run Matrix Studio with:
 
```gradle
cd matrixstudio
./gradlew run
```

It's easy too import the project in any IDE that supports gradle (IntelliJ, Eclipse with BuildShip and Netbeans with the gradle plugin).

To run Matrix Studio from the IDE, the main class is `matrixstudio.ui.MatrixStudio`.
If you're using a mac, you must add `-XstartOnFirstThread` on the JVM arguments ([reference](https://www.eclipse.org/swt/macosx/)).

## Authors

- Pascal Ballet ([Web Page](http://virtulab.univ-brest.fr/?page_id=32)).
- Jean-Charles Roger ([Web Page](http://minibilles.fr)).

## Documentation

- [Short introduction](http://virtulab.univ-brest.fr/MatrixStudioBook.pdf)
- [Course (in french)](http://virtulab.univ-brest.fr/GeneralCourse.pdf)

## MIT Licence

Copyright (c) <2017> <copyright P. Ballet & J.C Roger>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
