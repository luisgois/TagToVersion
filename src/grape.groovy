/**
 * Created by goi on 23/05/2017.
 */


// create and use a primitive array list
import org.apache.commons.collections.primitives.ArrayIntList

@Grapes([
        @Grab(group='commons-primitives', module='commons-primitives', version='1.0'),
        @Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='0.9.7'),
        @Grab(group='commons-primitives', module='commons-primitives', version='1.0'),
        @Grab(group='xerces', module='xercesImpl', version='2.11.0')])

def createEmptyInts() { new ArrayIntList() }

def ints = createEmptyInts()
ints.add(0, 42)
assert ints.size() == 1
assert ints.get(0) == 42

println "hello"