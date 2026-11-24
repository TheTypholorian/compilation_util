package net.typho.compilation_util.java.parser

import net.typho.compilation_util.java.ExtraOpcodes
import net.typho.compilation_util.java.ExtraOpcodes.toExtraAccessString
import net.typho.compilation_util.java.JPath
import net.typho.compilation_util.java.TypeArgumentRelativity
import net.typho.compilation_util.java.lexer.*
import net.typho.parsing_util.ParsingIterator
import net.typho.parsing_util.error.DataReadException
import org.objectweb.asm.Opcodes

@Suppress("UNCHECKED_CAST")
open class JavaParser {
    @JvmField
    val primitives = mutableSetOf("void", "boolean", "char", "byte", "short", "int", "float", "long", "double")

    open fun readAccessMask(iterator: ParsingIterator<JlToken>): Int {
        val modifier = iterator.peek()

        when (modifier.text.toString()) {
            "public" -> Opcodes.ACC_PUBLIC
            "protected" -> Opcodes.ACC_PROTECTED
            "private" -> Opcodes.ACC_PRIVATE
            else -> null
        }?.let {
            iterator.next()
            return it
        }

        return 0
    }

    open fun readModifierMask(iterator: ParsingIterator<JlToken>): Int {
        val modifier = iterator.peek()

        when (modifier.text.toString()) {
            "static" -> Opcodes.ACC_STATIC
            "final" -> Opcodes.ACC_FINAL
            "super" -> Opcodes.ACC_SUPER
            "synchronized" -> Opcodes.ACC_SYNCHRONIZED
            "open" -> Opcodes.ACC_OPEN
            "volatile" -> Opcodes.ACC_VOLATILE
            "bridge" -> Opcodes.ACC_BRIDGE
            "varargs" -> Opcodes.ACC_VARARGS
            "transient" -> Opcodes.ACC_TRANSIENT
            "native" -> Opcodes.ACC_NATIVE
            "abstract" -> Opcodes.ACC_ABSTRACT
            "strict" -> Opcodes.ACC_STRICT
            "synthetic" -> Opcodes.ACC_SYNTHETIC
            "mandated" -> Opcodes.ACC_MANDATED
            "module" -> Opcodes.ACC_MODULE
            "sealed" -> ExtraOpcodes.ACC_SEALED
            else -> null
        }?.let {
            iterator.next()
            return it
        }

        // don't ask questions
        // this is a perfect solution
        if (modifier.text.toString() == "non") {
            val fork = iterator.fork()
            fork.next()

            if (fork.hasNext() && (fork.next() as? JlPunctuation)?.char == '-') {
                if (fork.hasNext() && (fork.next() as? JlIdentifier)?.text.toString() == "sealed") {
                    return ExtraOpcodes.ACC_NON_SEALED
                }
            }
        }

        return 0
    }

    open fun validateClassAccess(access: Int): Int {
        val expected = Opcodes.ACC_PUBLIC or
                Opcodes.ACC_PRIVATE or
                Opcodes.ACC_PROTECTED or
                Opcodes.ACC_STATIC or
                Opcodes.ACC_FINAL or
                Opcodes.ACC_SUPER or
                Opcodes.ACC_INTERFACE or
                Opcodes.ACC_SYNTHETIC or
                Opcodes.ACC_ANNOTATION or
                Opcodes.ACC_ENUM or
                Opcodes.ACC_MODULE
        val extra = access and expected.inv()

        if (extra != 0) {
            throw DataReadException("Illegal class access '${extra.toExtraAccessString()}'")
        }

        return access
    }

    open fun validateInitializerAccess(access: Int): Int {
        val expected = Opcodes.ACC_STATIC
        val extra = access and expected.inv()

        if (extra != 0) {
            throw DataReadException("Illegal initializer access '${extra.toExtraAccessString()}'")
        }

        return access
    }

    open fun validateConstructorAccess(access: Int): Int {
        val expected = Opcodes.ACC_PUBLIC or
                Opcodes.ACC_PRIVATE or
                Opcodes.ACC_PROTECTED or
                Opcodes.ACC_VARARGS or
                Opcodes.ACC_SYNTHETIC or
                Opcodes.ACC_MANDATED
        val extra = access and expected.inv()

        if (extra != 0) {
            throw DataReadException("Illegal constructor access '${extra.toExtraAccessString()}'")
        }

        return access
    }

    open fun validateMethodAccess(access: Int): Int {
        val expected = Opcodes.ACC_PUBLIC or
                Opcodes.ACC_PRIVATE or
                Opcodes.ACC_PROTECTED or
                Opcodes.ACC_STATIC or
                Opcodes.ACC_FINAL or
                Opcodes.ACC_SYNCHRONIZED or
                Opcodes.ACC_BRIDGE or
                Opcodes.ACC_VARARGS or
                Opcodes.ACC_NATIVE or
                Opcodes.ACC_ABSTRACT or
                Opcodes.ACC_STRICT or
                Opcodes.ACC_SYNTHETIC or
                Opcodes.ACC_MANDATED
        val extra = access and expected.inv()

        if (extra != 0) {
            throw DataReadException("Illegal method access '${extra.toExtraAccessString()}'")
        }

        return access
    }

    open fun validateFieldAccess(access: Int): Int {
        val expected = Opcodes.ACC_PUBLIC or
                Opcodes.ACC_PRIVATE or
                Opcodes.ACC_PROTECTED or
                Opcodes.ACC_STATIC or
                Opcodes.ACC_FINAL or
                Opcodes.ACC_VOLATILE or
                Opcodes.ACC_TRANSIENT or
                Opcodes.ACC_SYNTHETIC or
                Opcodes.ACC_MANDATED
        val extra = access and expected.inv()

        if (extra != 0) {
            throw DataReadException("Illegal field access '${extra.toExtraAccessString()}'")
        }

        return access
    }

    open fun readAccess(iterator: ParsingIterator<JlToken>): Int {
        var access = readAccessMask(iterator)

        while (true) {
            val modifierMask = readModifierMask(iterator)

            if (modifierMask == 0) {
                break
            }

            val newAccess = access or modifierMask

            if (access == newAccess) {
                throw DataReadException("Duplicate modifier ${modifierMask.toExtraAccessString()}")
            }

            access = newAccess
        }

        return access
    }

    open fun readPath(iterator: ParsingIterator<JlToken>, allowPrimitive: Boolean): JPath {
        if (allowPrimitive) {
            val token = iterator.peek()

            if (token is JlIdentifier && token.text.toString() in primitives) {
                iterator.next()
                return JPath(token.text.toString())
            }
        }

        val path = mutableListOf<JlToken>()

        while (true) {
            val token = iterator.peek()

            if (token !is JlIdentifier) {
                break
            }

            iterator.next()
            path.add(token)

            if ((iterator.peekOrNull() as? JlPunctuation)?.char != '.') {
                break
            }

            iterator.next()
        }

        return JPath(path.joinToString(separator = ".") { it.text.toString() })
    }

    open fun readType(iterator: ParsingIterator<JlToken>, readAnnotations: Boolean, readTypeArguments: Boolean): JpType {
        return JpType(
            if (readAnnotations) {
                readAnnotations(iterator)
            } else {
                mutableListOf()
            },
            readPath(iterator, true),
            if (readTypeArguments && (iterator.peekOrNull() as? JlPunctuation)?.char == '<') {
                iterator.next()
                readTypeArguments(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")))
            } else {
                mutableListOf()
            }
        )
    }

    open fun readAnnotationValue(value: List<JlToken>): JpAnnotationValue<*> {
        val iterator = ParsingIterator(value)
        val value = when (val type = iterator.next()) {
            is JlNumber -> JpNumberAnnotationValue(type)
            is JlText -> JpTextAnnotationValue(type)
            is JlPunctuation if type.char == '@' -> JpNestedAnnotationValue(readAnnotation(iterator))
            is JlPunctuation if type.char == '{' -> {
                val body = ParsingIterator(iterator.readBrackets('{', '}', "annotation array contents"))
                val array = mutableListOf<JpAnnotationValue<*>>()

                while (body.hasNext()) {
                    array.add(readAnnotationValue(body.nextUntilOrEndConsideringBrackets(',', "annotation array value")))
                }

                JpArrayAnnotationValue(array)
            }
            else -> JpUnknownAnnotationValue(value)
        }

        if (iterator.hasNext()) {
            throw DataReadException()
        }

        return value
    }

    open fun readAnnotation(iterator: ParsingIterator<JlToken>): JpAnnotation {
        val anno = JpAnnotation(readPath(iterator, false), mutableListOf())

        val open = iterator.peek()

        if (open is JlPunctuation && open.char == '(') {
            iterator.next()
            var key: JlIdentifier? = null
            var value = iterator.readBrackets('(', ')', "annotation value")

            if (value.size >= 2 && value.first() is JlIdentifier && (value[1] as? JlPunctuation)?.char == '=') {
                key = value.first().cast()
                value = value.subList(2, value.size)
            }

            anno.values.add(key to readAnnotationValue(value))
        }

        return anno
    }

    open fun readAnnotations(iterator: ParsingIterator<JlToken>, list: MutableList<JpAnnotation> = mutableListOf()): MutableList<JpAnnotation> {
        while (true) {
            val at = iterator.peek()

            if ((at as? JlPunctuation)?.char == '@') {
                iterator.next()
                list.add(readAnnotation(iterator))
            } else {
                break
            }
        }

        return list
    }

    open fun readArgumentDeclaration(tokens: List<JlToken>): JpArgument {
        val name = tokens.last()

        if (name !is JlIdentifier) {
            throw DataReadException(tokens.toString())
        }

        val type = readType(ParsingIterator(tokens, 0 until (tokens.size - 1)), readAnnotations = true, readTypeArguments = true)
        return JpArgument(type, name)
    }

    open fun readArgumentListDeclaration(iterator: ParsingIterator<JlToken>, list: MutableList<JpArgument> = mutableListOf()): MutableList<JpArgument> {
        while (iterator.hasNext()) {
            list.add(readArgumentDeclaration(iterator.nextUntilOrEndConsideringBrackets(',', "argument list")))
        }

        return list
    }

    open fun readTypeArgument(iterator: ParsingIterator<JlToken>): JpTypeArgumentValue {
        var relativity: TypeArgumentRelativity? = null

        if ((iterator.peek() as? JlPunctuation)?.char == '?') {
            iterator.next()

            if (iterator.hasNext()) {
                relativity = when (iterator.next().cast<JlIdentifier>().text.toString()) {
                    "extends" -> TypeArgumentRelativity.EXTENDS
                    "super" -> TypeArgumentRelativity.SUPER
                    else -> throw DataReadException()
                }
            } else {
                return JpTypeArgumentValue(JpType(mutableListOf(), JPath("?"), mutableListOf()), null)
            }
        }

        val type = readType(iterator, readAnnotations = true, readTypeArguments = true)

        if (iterator.hasNext()) {
            throw DataReadException()
        }

        return JpTypeArgumentValue(type, relativity)
    }

    open fun readTypeArguments(iterator: ParsingIterator<JlToken>, list: MutableList<JpTypeArgumentValue> = mutableListOf()): MutableList<JpTypeArgumentValue> {
        while (iterator.hasNext()) {
            val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
            list.add(readTypeArgument(iterator))

            if (iterator.hasNext()) {
                throw DataReadException()
            }
        }

        return list
    }

    open fun readTypeArgumentsDeclaration(iterator: ParsingIterator<JlToken>, list: MutableList<JpTypeArgument> = mutableListOf()): MutableList<JpTypeArgument> {
        while (iterator.hasNext()) {
            val iterator = ParsingIterator(iterator.nextUntilOrEndConsideringBrackets(',', "type argument"))
            val argument = JpTypeArgument(iterator.next().cast(), mutableListOf())

            if (iterator.hasNext()) {
                when (val token = iterator.next()) {
                    is JlIdentifier if token.text.toString() == "extends" -> {
                        while (iterator.hasNext()) {
                            argument.parents.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                            if (iterator.hasNext()) {
                                val token = iterator.next()

                                if ((token as? JlPunctuation)?.char != '&') {
                                    throw DataReadException(token.toString())
                                }

                                if (!iterator.hasNext()) {
                                    throw DataReadException()
                                }
                            }
                        }
                    }
                    else -> throw DataReadException(token.toString())
                }
            }

            list.add(argument)
        }

        return list
    }

    open fun tryReadInitializer(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpInitializer? {
        val access = readAccess(iterator)

        if ((iterator.next() as? JlPunctuation)?.char != '{') {
            return null
        }

        return JpInitializer(
            validateInitializerAccess(access),
            iterator.readBrackets('{', '}', "method body").toMutableList()
        )
    }

    open fun tryReadRegularClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, parent: JpClass?): JpClass {
        val cls = JpClass(
            validateClassAccess(access),
            annotations,
            iterator.next().cast(),
            mutableListOf(),
            null,
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )
        val typeArgumentsOpen = iterator.peek()

        if (typeArgumentsOpen is JlPunctuation && typeArgumentsOpen.char == '<') {
            iterator.next()
            readTypeArgumentsDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), cls.typeArguments)
        }

        val extends = iterator.peek()

        if (extends is JlIdentifier && extends.text.toString() == "extends") {
            iterator.next()
            cls.superClass = readType(iterator, readAnnotations = true, readTypeArguments = true)
        }

        val implements = iterator.peek()

        if (implements is JlIdentifier && implements.text.toString() == "implements") {
            iterator.next()

            while (true) {
                cls.interfaces.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                val next = iterator.peek()

                if (next is JlPunctuation) {
                    when (next.char) {
                        ',' -> iterator.next()
                        '{' -> break
                        else -> throw DataReadException(next.char.toString())
                    }
                } else {
                    throw DataReadException(next.toString())
                }
            }
        }

        val permits = iterator.peek()

        if (permits is JlIdentifier && permits.text.toString() == "permits") {
            iterator.next()

            while (true) {
                cls.permits.add(readType(iterator, readAnnotations = true, readTypeArguments = false))

                val next = iterator.peek()

                if (next is JlPunctuation) {
                    when (next.char) {
                        ',' -> iterator.next()
                        '{' -> break
                        else -> throw DataReadException(next.char.toString())
                    }
                } else {
                    throw DataReadException(next.toString())
                }
            }
        }

        val bodyOpen = iterator.peek()

        if (bodyOpen is JlPunctuation && bodyOpen.char == '{') {
            iterator.next()
        } else {
            throw DataReadException(bodyOpen.toString())
        }

        val iterator = ParsingIterator(iterator.readBrackets('{', '}', "class body"))

        while (iterator.hasNext()) {
            cls.attributes.add(tryReadAttribute(iterator, cls) ?: TODO())
        }

        return cls
    }

    open fun tryReadAnnotationClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, parent: JpClass?): JpClass {
        TODO()
    }

    open fun tryReadEnumClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, parent: JpClass?): JpClass {
        TODO()
    }

    open fun tryReadInterfaceClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, parent: JpClass?): JpClass {
        TODO()
    }

    open fun tryReadRecordClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, parent: JpClass?): JpClass {
        TODO()
    }

    open fun tryReadClass(iterator: ParsingIterator<JlToken>, access: Int, annotations: MutableList<JpAnnotation>, type: JlToken, parent: JpClass?): JpClass? {
        if (type is JlPunctuation && type.char == '@') {
            val next = iterator.peek()

            if (next is JlIdentifier && next.text.toString() == "interface") {
                iterator.next()
                return tryReadAnnotationClass(iterator, access or Opcodes.ACC_ANNOTATION, annotations, parent)
            }
        }

        return when (type.text.toString()) {
            "class" -> tryReadRegularClass(iterator, access, annotations, parent)
            "enum" -> tryReadEnumClass(iterator, access or Opcodes.ACC_ENUM, annotations, parent)
            "interface" -> tryReadInterfaceClass(iterator, access or Opcodes.ACC_INTERFACE, annotations, parent)
            "record" -> tryReadRecordClass(iterator, access or Opcodes.ACC_RECORD, annotations, parent)
            else -> throw DataReadException("Unknown class type ${type.text}")
        }
    }

    open fun tryReadClass(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpClass? {
        val annotations = readAnnotations(iterator)
        val access = readAccess(iterator)
        val type = iterator.next()
        return tryReadClass(iterator, access, annotations, type, parent)
    }

    open fun tryReadMethod(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpMethod? {
        val annotations = readAnnotations(iterator)
        val access = readAccess(iterator)

        val typeArgumentsOpen = iterator.peek()
        val typeArguments = mutableListOf<JpTypeArgument>()

        if (typeArgumentsOpen is JlPunctuation && typeArgumentsOpen.char == '<') {
            iterator.next()
            readTypeArgumentsDeclaration(ParsingIterator(iterator.readBrackets('<', '>', "type argument list")), typeArguments)
        }

        val returnType = readType(iterator, readAnnotations = true, readTypeArguments = true)
        val name = iterator.next() as? JlIdentifier ?: return null

        val open = iterator.next()

        if (!(open is JlPunctuation && open.char == '(')) {
            return null
        }

        val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

        val exceptions = mutableListOf<JpType>()
        val token = iterator.peek()

        if (token is JlIdentifier && token.text.toString() == "throws") {
            iterator.next()

            while (true) {
                exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                val next = iterator.peek()

                if (next is JlPunctuation) {
                    when (next.char) {
                        ',' -> iterator.next()
                        else -> break
                    }
                } else {
                    throw DataReadException(next.toString())
                }
            }
        }

        val body = when (val next = (iterator.next().cast<JlPunctuation>()).char) {
            ';' -> null
            '{' -> iterator.readBrackets('{', '}', "method body").toMutableList()
            else -> throw DataReadException(next.toString())
        }

        return JpMethod(
            validateMethodAccess(access),
            annotations,
            typeArguments,
            returnType,
            arguments,
            name,
            exceptions,
            body
        )
    }

    open fun tryReadConstructor(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpConstructor? {
        val annotations = readAnnotations(iterator)
        val access = readAccess(iterator)
        val name = iterator.next() as? JlIdentifier ?: return null

        parent?.let {
            if (name.text.toString() != it.name.text.toString()) {
                return null
            }
        }

        val open = iterator.next()

        if (!(open is JlPunctuation && open.char == '(')) {
            return null
        }

        val arguments = readArgumentListDeclaration(ParsingIterator(iterator.readBrackets('(', ')', "argument list")))

        val exceptions = mutableListOf<JpType>()
        val token = iterator.peek()

        if (token is JlIdentifier && token.text.toString() == "throws") {
            iterator.next()

            while (true) {
                exceptions.add(readType(iterator, readAnnotations = true, readTypeArguments = true))

                val next = iterator.peek()

                if (next is JlPunctuation) {
                    when (next.char) {
                        ',' -> iterator.next()
                        else -> break
                    }
                } else {
                    throw DataReadException(next.toString())
                }
            }
        }

        val body = when (val next = (iterator.next().cast<JlPunctuation>()).char) {
            ';' -> throw DataReadException("Constructor must have body")
            '{' -> iterator.readBrackets('{', '}', "method body").toMutableList()
            else -> throw DataReadException(next.toString())
        }

        return JpConstructor(
            validateConstructorAccess(access),
            annotations,
            arguments,
            exceptions,
            body
        )
    }

    open fun tryReadField(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpField? {
        val annotations = readAnnotations(iterator)
        val access = readAccess(iterator)
        val type = readType(iterator, readAnnotations = true, readTypeArguments = true)
        val name = iterator.next() as? JlIdentifier ?: return null

        val initializerToken = iterator.next() as? JlPunctuation ?: return null // TODO

        return when (initializerToken.char) {
            ';' -> JpField(
                validateFieldAccess(access),
                annotations,
                type,
                name,
                null
            )
            '=' -> {
                val initializer = (iterator.nextUntil { value ->
                    (value as? JlPunctuation)?.char == ';'
                } ?: throw DataReadException("Unterminated initializer")).toMutableList()
                JpField(
                    validateFieldAccess(access),
                    annotations,
                    type,
                    name,
                    initializer
                )
            }
            else -> null
        }
    }

    open fun tryReadAttribute(iterator: ParsingIterator<JlToken>, parent: JpClass?): JpAttribute? {
        // sorted most likely first
        // TODO what if more than one type here works?
        var fork = iterator.fork()
        tryReadMethod(fork, parent)?.let {
            fork.merge()
            return it
        }

        fork = iterator.fork()
        tryReadField(fork, parent)?.let {
            fork.merge()
            return it
        }

        fork = iterator.fork()
        tryReadConstructor(fork, parent)?.let {
            fork.merge()
            return it
        }

        fork = iterator.fork()
        tryReadInitializer(fork, parent)?.let {
            fork.merge()
            return it
        }

        fork = iterator.fork()
        tryReadClass(fork, parent)?.let {
            fork.merge()
            return it
        }

        return null
    }

    open fun readFile(tokens: List<JlToken>): JpFile {
        val iterator = ParsingIterator(tokens.filter { it !is JlComment })

        var pkg: JPath? = null
        val imports = mutableListOf<JPath>()

        val token = iterator.peek()

        if (token is JlIdentifier && token.text.toString() == "package") {
            iterator.next()

            val path = iterator.nextUntil { value ->
                (value as? JlPunctuation)?.char == ';'
            } ?: throw DataReadException()
            val iterator = ParsingIterator(path)
            pkg = readPath(iterator, false)

            if (iterator.hasNext()) {
                throw DataReadException()
            }
        }

        while (true) {
            val token = iterator.peek()

            if (token is JlIdentifier && token.text.toString() == "import") {
                iterator.next()

                val path = iterator.nextUntil { value ->
                    (value as? JlPunctuation)?.char == ';'
                } ?: throw DataReadException()
                val iterator = ParsingIterator(path)
                imports.add(readPath(iterator, false))
            } else {
                break
            }
        }

        val file = JpFile(pkg, imports, mutableListOf())

        while (iterator.hasNext()) {
            file.classes.add(tryReadClass(iterator, null) ?: throw DataReadException(iterator.peek().toString()))
        }

        return file
    }
}