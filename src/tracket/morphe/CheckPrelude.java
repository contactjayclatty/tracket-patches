package tracket.morphe;

import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.DexFile;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;
import java.io.File;
import java.util.List;

/** Prints the start of the patched grant method. */
public final class CheckPrelude {
    public static void main(String[] args) throws Exception {
        File dex = new File(args[0]);
        DexFile file = DexFileFactory.loadDexFile(dex, Opcodes.getDefault());
        for (ClassDef classDef : file.getClasses()) {
            for (Method method : classDef.getMethods()) {
                if (method.getImplementation() == null || !has(method, "purchaseTime") || !has(method, "SHA-256")) {
                    continue;
                }
                if (!"Ljava/util/List;".equals(String.valueOf(method.getParameterTypes().get(0)))) {
                    continue;
                }
                System.out.println(method.getDefiningClass() + "->" + method.getName());
                int n = 0;
                for (Instruction instruction : method.getImplementation().getInstructions()) {
                    System.out.println(instruction.getOpcode() + " " + instruction);
                    if (++n >= 20) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean has(Method method, String value) {
        for (Instruction instruction : method.getImplementation().getInstructions()) {
            if (instruction instanceof ReferenceInstruction) {
                Object reference = ((ReferenceInstruction) instruction).getReference();
                if (reference instanceof StringReference && value.equals(((StringReference) reference).getString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
