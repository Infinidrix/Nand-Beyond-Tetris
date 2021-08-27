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

main: $(MainClassesJar)
	./InstallDir/CPUEmulator.sh

$(Lib)/Hack.jar: $(HackPackageSource)
	javac HackPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/Hack.jar $(Lib)/Hack/ComputerParts/*.class $(Lib)/Hack/Controller/*.class $(Lib)/Hack/Events/*.class $(Lib)/Hack/Translators/*.class $(Lib)/Hack/Utilities/*.class

$(Lib)/HackGUI.jar: $(HackGUIPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/HackGUI.jar $(Lib)/HackGUI/*.class

$(Lib)/Compilers.jar: $(CompilersPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/*/*/*.java CompilersPackageSource/Hack/*/*.java -d $(Lib)/
	jar cf $(Lib)/Compilers.jar $(Lib)/Hack/Assembler/*.class $(Lib)/Hack/VirtualMachine/*.class

$(Lib)/Simulators.jar: $(SimulatorsPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java -d $(Lib)/
# 	jar cf Simulators.jar Hack/CPUEmulator/*.class Hack/Gates/*.class Hack/HardwareSimulator/*.class

$(Lib)/SimulatorsGUI.jar: $(SimulatorsGUIPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java -d $(Lib)/
	jar cf $(Lib)/SimulatorsGUI.jar $(Lib)/SimulatorsGUI/*.class

$(BuiltinChipsJar): $(BuiltInChipsSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java -d InstallDir/temp/
	jar cf InstallDir/temp/builtInChips/Chips.jar InstallDir/temp/builtInChips/*.class
	rsync -a InstallDir/temp/builtInChips/ InstallDir/builtInChips/
	rm -r InstallDir/temp/

$(BuiltinVMCodeJar): $(BuiltInVMCodeSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java BuiltInVMCodeSource/*.java -d InstallDir/temp/
	jar cf $(BuiltinVMCodeJar) InstallDir/temp/builtInVMCode/*.class
	rsync -a InstallDir/temp/builtInVMCode/ InstallDir/builtInVMCode/
	rm -r InstallDir/temp/

$(MainClassesJar): $(MainClassesSource) $(SimulatorsGUIPackageSource) $(SimulatorsPackageSource) $(BuiltInVMCodeSource) $(BuiltInChipsSource)  $(CompilersPackageSource)  $(HackGUIPackageSource) $(HackPackageSource)
	javac HackGUIPackageSource/HackGUI/*.java HackPackageSource/Hack/*/*.java CompilersPackageSource/Hack/*/*.java SimulatorsPackageSource/Hack/*/*.java SimulatorsGUIPackageSource/SimulatorsGUI/*.java BuiltInChipsSource/*.java  BuiltInVMCodeSource/*.java MainClassesSource/*.java -d InstallDir/bin/classes/
	cd InstallDir/bin/classes; jar cf MainClasses.jar *.class Hack/*/*.class HackGUI/*.class SimulatorsGUI/*.class
	rm -r InstallDir/bin/classes/*/