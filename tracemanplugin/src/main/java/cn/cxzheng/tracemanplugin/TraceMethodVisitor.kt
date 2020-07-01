package cn.cxzheng.tracemanplugin

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * Create by cxzheng on 2019/6/4
 * Method Visitor
 */
class TraceMethodVisitor(
    api: Int, mv: MethodVisitor?, access: Int,
    name: String?, desc: String?, className: String?,
    var traceConfig: Config
) : AdviceAdapter(api, mv, access, name, desc) {

    private var methodName: String? = null
    private var name: String? = null
    private var className: String? = null
    private val maxSectionNameLength = 30


    init {
        val traceMethod = TraceMethod.create(0, access, className, name, desc)
        this.methodName = traceMethod.getMethodNameText()
        this.className = className
        this.name = name

    }


    override fun onMethodEnter() {
        super.onMethodEnter()
        val methodName = generatorMethodName()
        mv.visitLdcInsn(generatorMethodName())
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/String",
            "length",
            "()I",
            false
        )
        val l1 = Label()
        mv.visitJumpInsn(Opcodes.IFNE, l1)
        val l2 = Label()
        mv.visitLabel(l2)
        mv.visitLdcInsn(generatorMethodName())
        mv.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/String",
            "toString",
            "()I",
            false
        )
        mv.visitInsn(Opcodes.POP)
        mv.visitLabel(l1)
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        if (traceConfig.mIsNeedLogTraceInfo) {
            println("MethodTraceMan-trace-method: ${methodName ?: "emptyName"}")
        }
    }

    override fun onMethodExit(opcode: Int) {
        mv.visitLdcInsn(generatorMethodName())
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "java/lang/String",
            "toString",
            "()I",
            false
        )
        mv.visitInsn(POP);
    }

    private fun generatorMethodName(): String? {
        var sectionName = methodName
        var length = sectionName?.length ?: 0
        if (length > maxSectionNameLength && !sectionName.isNullOrBlank()) {
            // 先去掉参数
            val parmIndex = sectionName.indexOf('(')
            sectionName = sectionName.substring(0, parmIndex)
            // 如果依然更大，直接裁剪
            length = sectionName.length
            if (length > 127) {
                sectionName = sectionName.substring(length - maxSectionNameLength)
            }
        }
        return sectionName
    }

}