package com.driver733.infixfunctionsgenerator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("com.driver733.infixfunctionsgenerator.Infix")
@SupportedOptions(InfixAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class InfixAnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private const val RECEIVER = "receiver"
    }

    override fun process(annotations: MutableSet<out TypeElement>?, env: RoundEnvironment?) =
            generatedSourcesDirPath()
                    .also { if (it == null) return false }
                    .let { src ->
                        env?.getElementsAnnotatedWith(Infix::class.java)?.forEach { method ->
                            if (!process(method as ExecutableElement, env, src)) {
                                return false
                            }
                        }
                    }.let {
                        true
                    }

    private fun process(method: ExecutableElement, env: RoundEnvironment, src: String?) =
            infixMethodsNames(
                    method,
                    method.parameters.count()
            ).let { infixFunsNames ->
                method.parameters
                        .also {
                            if (methodHasNoParams(it, method) || infixFunsNames == null) {
                                return false
                            }
                        }.also {
                            generate(
                                    method,
                                    methodClassName(env, method),
                                    infixFunsNames!!,
                                    it,
                                    File(src!!).apply { mkdir() }
                            )
                        }.let {
                            true
                        }
            }

    private fun generatedSourcesDirPath(): String? =
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                    .let {
                        if (it == null) {
                            processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Can't find the target directory for generated Kotlin files"
                            )
                            null
                        } else {
                            it
                        }
                    }

    private fun infixMethodsNames(method: ExecutableElement, paramsCount: Int) =
            method.getAnnotation(Infix::class.java)
                    .intermediateMethodsNames
                    .toList()
                    .let { infixFunsNames ->
                        if (infixFunsNames.isNotEmpty() && infixFunsNames.size != paramsCount - 1) {
                            processingEnv.messager.printMessage(
                                    Diagnostic.Kind.ERROR,
                                    "Specified methods names array size must equal the number of the method params minus 1,  element: $method "
                            )
                            null
                        } else {
                            infixFunsNames
                        }
                    }

    private fun methodClassName(env: RoundEnvironment, method: ExecutableElement) =
            env.rootElements
                    .filter { it.kind == ElementKind.CLASS }
                    .first { it.enclosedElements.contains(method) }
                    .simpleName
                    .toString()

    private fun methodHasNoParams(params: List<VariableElement>, method: Element?) =
            if (params.count() == 0) {
                processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Can only be applied to functions with at least one parameter,  element: $method "
                )
                true
            } else {
                false
            }

    private fun generate(
            method: ExecutableElement,
            methodClassName: String,
            infixFunsNames: List<String>,
            params: List<VariableElement>,
            fileDir: File
    ) = FileSpec.builder(
            "${method.simpleName.capitalize()}InfixExtensions"
    ).addImport(
            processingEnv.elementUtils.getPackageOf(method).toString(),
            methodClassName
    ).also { file ->
        generate(
                params,
                file,
                className(methodClassName),
                method.simpleName.toString(),
                customInitialMethodName(method),
                method.returnType,
                infixFunsNames
        )
    }.also { file ->
        file.build().writeTo(fileDir)
    }

    private fun generate(
            params: List<VariableElement>,
            file: FileSpec.Builder,
            receiver: ClassName,
            methodName: String,
            customMethodName: String?,
            methodReturnType: TypeMirror,
            infixFunsNames: List<String>
    ) = with(file) {
        if (params.count() == 1) {
            addFunction(
                    funSpecInitial(receiver, params, methodName, customMethodName, methodReturnType)
            )
        } else {
            addFunction(
                    initialInfixFun(receiver, methodName, customMethodName, params)
            )
            (0 until params.lastIndex).forEach { i ->
                if (i < params.lastIndex - 1) {
                    addType(
                            typeSpecIntermediate(
                                    i,
                                    receiver,
                                    params,
                                    methodName,
                                    infixFunsNames.getOrNull(i),
                                    className("${methodName.capitalize()}Step${i + 2}")
                            )
                    )
                } else {
                    addType(
                            typeSpecIntermediate(
                                    i,
                                    receiver,
                                    params,
                                    methodName,
                                    infixFunsNames.getOrNull(i),
                                    methodReturnType
                            )
                    )
                }
            }
        }
    }

    private fun customInitialMethodName(method: ExecutableElement) =
            method.getAnnotation(Infix::class.java)
                    .firstMethodName
                    .let {
                        if (it.isEmpty()) null else it
                    }

    private fun typeSpecIntermediate(
            i: Int,
            receiver: ClassName,
            params: List<VariableElement>,
            methodName: String,
            infixFunName: String?,
            methodReturnType: TypeMirror
    ) = TypeSpec
            .classBuilder("${methodName.capitalize()}Step${i + 1}")
            .addProperty(
                    propertySpecReceiver(receiver)
            ).addProperties(
                    mapToPropertySpec(params.dropLast(1))
            ).primaryConstructor(
                    funSpecReceiver(receiver, params.dropLast(1))
            ).addFunction(
                    funSpecIntermediate(
                            params[i + 1],
                            params.joinToString { it.simpleName },
                            methodName,
                            infixFunName,
                            methodReturnType
                    )
            ).build()

    private fun typeSpecIntermediate(
            i: Int,
            receiver: ClassName,
            params: List<VariableElement>,
            methodName: String,
            infixFunName: String?,
            nextClassName: ClassName
    ) = TypeSpec
            .classBuilder("${methodName.capitalize()}Step${i + 1}")
            .addProperty(
                    propertySpecReceiver(receiver)
            ).addProperties(
                    mapToPropertySpec(params.take(i + 1))
            ).primaryConstructor(
                    funSpecReceiver(receiver, params.take(i + 1))
            ).addFunction(
                    funSpecInitial(i, params, infixFunName, nextClassName)
            ).build()

    private fun mapToPropertySpec(params: List<VariableElement>) =
            params.map {
                propertySpec(it)
            }

    private fun funSpecIntermediate(
            param: VariableElement,
            paramsNames: String,
            methodName: String,
            infixFunName: String?,
            methodReturnType: TypeMirror
    ) = FunSpec.builder(infixFunName ?: "and")
            .addModifiers(KModifier.INFIX)
            .addParameter(
                    param.name(),
                    param.kotlinType()
            ).addStatement(
                    "return $RECEIVER.$methodName($paramsNames)"
            ).build()

    private fun funSpecReceiver(receiver: ClassName, params: List<VariableElement>) =
            FunSpec.constructorBuilder()
                    .addParameter(RECEIVER, receiver)
                    .addParameters(params.map { parameterSpec(it) })
                    .build()

    private fun funSpecInitial(i: Int, params: List<VariableElement>, infixFunName: String?, nextClassName: ClassName) =
            FunSpec.builder(infixFunName ?: "and")
                    .addModifiers(KModifier.INFIX)
                    .addParameter(
                            parameterSpec(params[i + 1])
                    ).addStatement(
                            "return %T($RECEIVER, ${
                            params.take(i + 2)
                                    .joinToString {
                                        it.simpleName
                                    }
                            })",
                            nextClassName
                    ).returns(
                            nextClassName
                    ).build()


    private fun propertySpecReceiver(receiver: ClassName) =
            PropertySpec.builder(RECEIVER, receiver)
                    .initializer(RECEIVER)
                    .build()

    private fun parameterSpec(param: VariableElement) =
            ParameterSpec.builder(
                    param.name(),
                    param.kotlinType()
            ).build()

    private fun propertySpec(param: VariableElement) =
            PropertySpec.builder(
                    param.name(),
                    param.kotlinType()
            ).initializer(
                    param.name()
            ).build()

    private fun initialInfixFun(
            receiver: ClassName,
            methodName: String,
            customMethodName: String?,
            params: List<VariableElement>
    ) = className("${methodName.capitalize()}Step1")
            .let { nextStepClass ->
                FunSpec.builder(customMethodName ?: methodName)
                        .addModifiers(KModifier.INFIX)
                        .receiver(receiver)
                        .addParameter(params.first().name(), params.first().kotlinType())
                        .addStatement("return %T(this, ${params.first().simpleName})", nextStepClass)
                        .returns(nextStepClass)
                        .build()

            }

    private fun funSpecInitial(
            receiver: ClassName,
            params: List<VariableElement>,
            methodName: String,
            customMethodName: String?,
            methodReturnType: TypeMirror
    ) = FunSpec.builder(customMethodName ?: methodName)
            .addModifiers(KModifier.INFIX)
            .receiver(receiver)
            .addParameter(
                    parameterSpec(params.first())
            ).addStatement(
                    "return $methodName(${params.first().simpleName})"
            ).returns(
                    methodReturnType.asTypeName().toKotlinType()
            ).build()

    private fun Name.capitalize() = this.toString().capitalize()

    private fun FileSpec.Companion.builder(fileName: String) = builder("", fileName)

    private fun className(vararg simpleNames: String) = ClassName("", *simpleNames)

    private fun VariableElement.name() = this.simpleName.toString()

    private fun VariableElement.kotlinType() = this.asType().asTypeName().toKotlinType()

    private fun TypeName.toKotlinType(): TypeName =
            when (this) {
                is ParameterizedTypeName -> {
                    (rawType.toKotlinType() as ClassName).parameterizedBy(
                            *typeArguments.map {
                                it.toKotlinType()
                            }.toTypedArray()
                    )
                }
                is WildcardTypeName -> {
                    val type =
                            if (inTypes.isNotEmpty()) WildcardTypeName.consumerOf(inTypes[0].toKotlinType())
                            else WildcardTypeName.producerOf(outTypes[0].toKotlinType())
                    type
                }
                else -> {
                    val className = JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
                    if (className == null) {
                        this
                    } else {
                        ClassName.bestGuess(className)
                    }
                }
            }

}

