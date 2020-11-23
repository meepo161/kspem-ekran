package ru.avem.ekran.entities

import javafx.beans.property.DoubleProperty
import javafx.beans.property.StringProperty

data class TableValuesTest1(
    var descriptor: StringProperty,
    var resistanceAB: StringProperty,
    var resistanceBC: StringProperty,
    var resistanceCA: StringProperty,
    var result: StringProperty
)

data class TableValuesTest2(
    var descriptor: StringProperty,
    var resistanceR: StringProperty,
    var result: StringProperty
)

data class TableValuesTest3(
    var descriptor: StringProperty,
    var voltage: StringProperty,
    var current: StringProperty,
    var result: StringProperty
)

data class TableValuesTest4(
    var descriptor: StringProperty,
    var resistanceInductiveAB: StringProperty,
    var resistanceInductiveBC: StringProperty,
    var resistanceInductiveCA: StringProperty,
    var result: StringProperty
)

data class TableValuesTest5(
    var descriptor: StringProperty,
    var resistanceR: StringProperty,
    var resistanceL: StringProperty,
    var result: StringProperty
)

data class TableValuesTest6(
    var descriptor: StringProperty,
    var R: StringProperty,
    var L: StringProperty,
    var C: StringProperty,
    var DCR: StringProperty,
    var result: StringProperty
)

data class TableValuesResult(
    var descriptor: StringProperty,
    var resistanceCoil1: DoubleProperty,
    var resistanceCoil2: DoubleProperty,
    var result1: StringProperty,
    var resistanceContactGroup1: DoubleProperty,
    var resistanceContactGroup2: DoubleProperty,
    var resistanceContactGroup3: DoubleProperty,
    var resistanceContactGroup4: DoubleProperty,
    var resistanceContactGroup5: DoubleProperty,
    var resistanceContactGroup6: DoubleProperty,
    var resistanceContactGroup7: DoubleProperty,
    var resistanceContactGroup8: DoubleProperty,
    var result2: StringProperty,
    var voltageMin: DoubleProperty,
    var result3: StringProperty,
    var voltageMax: DoubleProperty,
    var result4: StringProperty,
    var time: DoubleProperty,
    var result5: StringProperty
)
