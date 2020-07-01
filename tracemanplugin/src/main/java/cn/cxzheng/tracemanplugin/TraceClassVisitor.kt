package cn.cxzheng.tracemanplugin

import com.sun.org.apache.bcel.internal.generic.RETURN
import jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


/**
 * Create by cxzheng on 2019/6/4
 * Class Visitor
 */
class TraceClassVisitor(api: Int, cv: ClassVisitor?, var traceConfig: Config) :
    ClassVisitor(api, cv) {

    private var className: String? = null
    private var isABSClass = false
    private var isBeatClass = false
    private var isConfigTraceClass = false

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)

        this.className = name
        //抽象方法或者接口
        if (access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0) {
            this.isABSClass = true
        }

        //插桩代码所属类
        val resultClassName = name?.replace(".", "/")
        if (resultClassName == traceConfig.mBeatClass) {
            this.isBeatClass = true
        }

        //是否是配置的需要插桩的类
        name?.let { className ->
            isConfigTraceClass = traceConfig.isConfigTraceClass(className)
        }

        val isNotNeedTraceClass = isABSClass || isBeatClass || !isConfigTraceClass
        if (traceConfig.mIsNeedLogTraceInfo && !isNotNeedTraceClass) {
            println("MethodTraceMan-trace-class: ${className ?: "unknown"}")
            addConfuseMethod(name)
        }

    }

    private fun addConfuseMethod(name: String?): String? {
        if (name.isNullOrEmpty()) {
            return null
        }
        var target = name.replace("/", "")
        if (target.length > 20) {
            target = target.substring(0, 20)
        }
        val methodName = "confuse$target"
        val mv = cv.visitMethod(ACC_PUBLIC, methodName, "()V", null, null)
        mv.visitCode()
        val l0 = Label()
        mv.visitLabel(l0)
        mv.visitInsn(Opcodes.RETURN)
        val l1 = Label()
        mv.visitLabel(l1)
        mv.visitEnd()
        return methodName
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val isConstructor = isConstructor(name)
        val isPublic = access and Opcodes.ACC_PUBLIC > 0
        return if (isABSClass || isBeatClass || !isConfigTraceClass || isConstructor || !isPublic) {
            super.visitMethod(access, name, desc, signature, exceptions)
        } else {
            val mv = cv.visitMethod(access, name, desc, signature, exceptions)
            TraceMethodVisitor(api, mv, access, name, desc, className, traceConfig)
        }
    }

    private fun isConstructor(methodName: String?): Boolean {
        return methodName?.contains("<init>") ?: false
    }
}