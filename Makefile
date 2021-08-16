Lib = InstallDir/bin/lib
HackPackageSource = $(wildcard HackPackageSource/Hack/*/*.java)
HackGUIPackageSource = $(wildcard HackGUIPackageSource/HackGUI/*.java)
CompilersPackageSource = $(wildcard CompilersPackageSource/Hack/*/*.java)
SimulatorsPackageSource = $(wildcard SimulatorsPackageSource/Hack/*/*.java)
SimulatorsGUIPackageSource = $(wildcard SimulatorsGUIPackageSource/SimulatorsGUI/*.java)
BuiltinChipsSource = $(wildcard BuiltInChipsSource/*.java )
BuiltinChipsJar = InstallDir/builtInChips/Chips.jar
BuiltinVMCodeSource = $(wildcard BuiltInVMCodeSource/*.java)
BuiltinVMCodeJar = InstallDir/builtInVMCode/VMCode.jar
MainClassesSource = $(wildcard MainClassesSource/*.java)
MainClassesJar = InstallDir/bin/classes/MainClasses.jar

main: $(Lib)/Hack.jar $(Lib)/HackGUI.jar $(Lib)/Compilers.jar $(Lib)/Simulators.jar $(Lib)/SimulatorsGUI.jar $(BuiltinChipsJar) $(BuiltinVMCodeJar) $(MainClassesJar)
	./InstallDir/CPUEmulator.sh

$(Lib)/Hack.jar: $(HackPackageSource)
	javac HackPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/Hack.jar $(Lib)/Hack/ComputerParts/*.class $(Lib)/Hack/Controller/*.class $(Lib)/Hack/Events/*.class $(Lib)/Hack/Translators/*.class $(Lib)/Hack/Utilities/*.class

$(Lib)/HackGUI.jar: $(HackGUIPackageSource) $(HackPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/HackGUI.jar $(Lib)/HackGUI/*.class

$(Lib)/Compilers.jar: $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/*/*/*.java CompilersPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/Compilers.jar $(Lib)/Hack/Assembler/*.class $(Lib)/Hack/VirtualMachine/*.class

$(Lib)/Simulators.jar: $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource) $(SimulatorsPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/Simulators.jar $(Lib)/Hack/CPUEmulator/*.class $(Lib)/Hack/Gates/*.class $(Lib)/Hack/HardwareSimulator/*.class

$(Lib)/SimulatorsGUI.jar: $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource) $(SimulatorsPackageSource) $(SimulatorsGUIPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java -d $(Lib)/
	jar cf $(Lib)/SimulatorsGUI.jar $(Lib)/SimulatorsGUI/*.class

$(BuiltinChipsJar): $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource) $(SimulatorsPackageSource) $(SimulatorsGUIPackageSource) $(BuiltInChipsSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java -d InstallDir/temp/
	jar cf InstallDir/temp/builtInChips/Chips.jar InstallDir/temp/builtInChips/*.class
	rsync -a InstallDir/temp/builtInChips/ InstallDir/builtInChips/
	rm -r InstallDir/temp/

$(BuiltinVMCodeJar): $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource) $(SimulatorsPackageSource) $(SimulatorsGUIPackageSource) $(BuiltInChipsSource) $(BuiltInVMCodeSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java BuiltInVMCodeSource/*.java -d InstallDir/temp/
	jar cf $(BuiltinVMCodeJar) InstallDir/temp/builtInVMCode/*.class
	rsync -a InstallDir/temp/builtInVMCode/ InstallDir/builtInVMCode/
	rm -r InstallDir/temp/

$(MainClassesJar): $(HackGUIPackageSource) $(HackPackageSource) $(CompilersPackageSource) $(SimulatorsPackageSource) $(SimulatorsGUIPackageSource) $(BuiltInChipsSource) $(BuiltInVMCodeSource) $(MainClassesSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java  BuiltInVMCodeSource/*.java MainClassesSource/*.java -d InstallDir/bin/classes/
	jar cf $(MainClassesJar) InstallDir/bin/classes/*.class