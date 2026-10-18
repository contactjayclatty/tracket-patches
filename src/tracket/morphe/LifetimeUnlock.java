/*
 * Copyright (C) 2026 Mara Quill
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tracket.morphe;

import app.morphe.patcher.patch.ApkFileType;
import app.morphe.patcher.patch.BytecodePatch;
import app.morphe.patcher.patch.BytecodePatchBuilder;
import app.morphe.patcher.patch.BytecodePatchContext;
import app.morphe.patcher.patch.Compatibility;
import app.morphe.patcher.patch.AppTarget;
import app.morphe.patcher.patch.PatchException;
import app.morphe.patcher.patch.PatchKt;
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass;
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod;
import com.android.tools.smali.dexlib2.Opcode;
import com.android.tools.smali.dexlib2.builder.BuilderInstruction;
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11n;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21s;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c;
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c;
import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.iface.Method;
import com.android.tools.smali.dexlib2.iface.instruction.Instruction;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.StringReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableFieldReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference;
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableTypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/** Morphe patch for Tracket 0.9.38. Forces the client lifetime grant. */
public final class LifetimeUnlock {
    public static final BytecodePatch UNLOCK_LIFETIME = PatchKt.bytecodePatch(
            "Unlock lifetime",
            "Seeds a lifetime entitlement grant and skips the Play license check.",
            true,
            new Build());

    private LifetimeUnlock() {}

    private static final class Build implements Function1<BytecodePatchBuilder, Unit> {
        @Override
        public Unit invoke(BytecodePatchBuilder builder) {
            List<AppTarget> targets = new ArrayList<AppTarget>();
            targets.add(new AppTarget("0.9.38", false, null));
            Compatibility compat = new Compatibility(
                    "com.tracket.app",
                    "Tracket",
                    "Tracket",
                    ApkFileType.APK,
                    (Integer) null,
                    Collections.<String>emptySet(),
                    targets);
            builder.compatibleWith(compat);
            builder.execute(new Execute());
            return Unit.INSTANCE;
        }
    }

    private static final class Execute implements Function1<BytecodePatchContext, Unit> {
        @Override
        public Unit invoke(BytecodePatchContext context) {
            skipLicense(context);
            forceGrant(context);
            return Unit.INSTANCE;
        }
    }

    private static void skipLicense(BytecodePatchContext context) {
        MutableMethod method = findMethod(
                context,
                "Lcom/pairip/licensecheck/LicenseClient;",
                "checkLicense",
                "V");
        if (method == null) {
            throw fail("LicenseClient.checkLicense was not found");
        }
        MutableMethodImplementation impl = method.getImplementation();
        if (impl == null) {
            throw fail("LicenseClient.checkLicense has no body");
        }
        impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));
    }

    private static void forceGrant(BytecodePatchContext context) {
        MutableMethod method = null;
        String owner = null;
        for (ClassDef classDef : context.getAllClassesWithString("purchaseTime")) {
            MutableClass mutable = context.mutableClassDefBy(classDef.getType());
            for (Method candidate : mutable.getMethods()) {
                if (!"V".equals(candidate.getReturnType())) {
                    continue;
                }
                List<? extends CharSequence> params = candidate.getParameterTypes();
                if (params.size() != 1 || !"Ljava/util/List;".equals(params.get(0).toString())) {
                    continue;
                }
                if (!hasString(candidate, "purchaseTime") || !hasString(candidate, "SHA-256")) {
                    continue;
                }
                method = (MutableMethod) candidate;
                owner = candidate.getDefiningClass();
            }
        }
        if (method == null || owner == null) {
            throw fail("Tracket grant method was not found");
        }
        MutableMethodImplementation impl = method.getImplementation();
        if (impl == null || impl.getRegisterCount() < 7) {
            throw fail("Tracket grant method is too small to patch");
        }
        List<BuilderInstruction> code = grantPrelude(owner);
        int at = 0;
        for (BuilderInstruction instruction : code) {
            impl.addInstruction(at, instruction);
            at++;
        }
    }

    private static List<BuilderInstruction> grantPrelude(String owner) {
        List<BuilderInstruction> code = new ArrayList<BuilderInstruction>();
        code.add(sget(0, "Lql3;", "D", "Lql3;"));
        code.add(constString(4, "SHA-256"));
        code.add(invokeStatic(1, 4, 0, 0, 0, 0,
                "Ljava/security/MessageDigest;", "getInstance",
                Collections.singletonList("Ljava/lang/String;"),
                "Ljava/security/MessageDigest;"));
        code.add(moveResultObject(4));
        code.add(sget(5, "Lxb6;", "z", "Le2;"));
        code.add(new BuilderInstruction21s(Opcode.CONST_16, 6, 0x10));
        code.add(new BuilderInstruction22c(Opcode.NEW_ARRAY, 6, 6, new ImmutableTypeReference("[B")));
        code.add(invokeVirtual(1, 5, 0, 0, 0, 0, "Le2;", "g", Collections.<String>emptyList(), "Ljava/util/Random;"));
        code.add(moveResultObject(5));
        code.add(invokeVirtual(2, 5, 6, 0, 0, 0, "Ljava/util/Random;", "nextBytes",
                Collections.singletonList("[B"), "V"));
        code.add(invokeVirtual(2, 4, 6, 0, 0, 0, "Ljava/security/MessageDigest;", "update",
                Collections.singletonList("[B"), "V"));
        code.add(invokeVirtual(1, 4, 0, 0, 0, 0, "Ljava/security/MessageDigest;", "digest",
                Collections.<String>emptyList(), "[B"));
        code.add(moveResultObject(4));
        code.add(iput(Opcode.IPUT_OBJECT, 4, 0, "Lql3;", "B", "Ljava/lang/Object;"));
        code.add(new BuilderInstruction11n(Opcode.CONST_4, 2, 0));
        code.add(iput(Opcode.IPUT_OBJECT, 2, 0, "Lql3;", "C", "Ljava/lang/Object;"));
        code.add(new BuilderInstruction11n(Opcode.CONST_4, 1, 0));
        code.add(iput(Opcode.IPUT, 1, 0, "Lql3;", "A", "I"));
        code.add(sget(0, owner, "b", "Luy7;"));
        code.add(new BuilderInstruction21c(Opcode.NEW_INSTANCE, 4, new ImmutableTypeReference("Ljava/util/Date;")));
        code.add(invokeStatic(0, 0, 0, 0, 0, 0, "Ljava/lang/System;", "currentTimeMillis",
                Collections.<String>emptyList(), "J"));
        code.add(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, 5));
        code.add(invokeDirect(3, 4, 5, 6, 0, 0, "Ljava/util/Date;", "<init>",
                Collections.singletonList("J"), "V"));
        List<String> twoObjects = new ArrayList<String>();
        twoObjects.add("Ljava/lang/Object;");
        twoObjects.add("Ljava/lang/Object;");
        code.add(invokeVirtual(3, 0, 2, 4, 0, 0, "Luy7;", "l", twoObjects, "Z"));
        code.add(invokeStatic(0, 0, 0, 0, 0, 0, owner, "b", Collections.<String>emptyList(), "V"));
        code.add(sget(0, "Lmr;", "a", "Lk01;"));
        code.add(new BuilderInstruction21c(Opcode.NEW_INSTANCE, 4, new ImmutableTypeReference("Lsq;")));
        code.add(new BuilderInstruction11n(Opcode.CONST_4, 5, 2));
        code.add(new BuilderInstruction21s(Opcode.CONST_16, 6, 8));
        List<String> sqInit = new ArrayList<String>();
        sqInit.add("I");
        sqInit.add("I");
        sqInit.add("Lqp1;");
        code.add(invokeDirect(4, 4, 5, 6, 2, 0, "Lsq;", "<init>", sqInit, "V"));
        code.add(new BuilderInstruction11n(Opcode.CONST_4, 5, 3));
        List<String> launch = new ArrayList<String>();
        launch.add("Lor1;");
        launch.add("Lgr1;");
        launch.add("Lkotlinx/coroutines/CoroutineStart;");
        launch.add("Lih3;");
        launch.add("I");
        code.add(invokeStatic(5, 0, 2, 2, 4, 5, "Ltz7;", "g", launch, "Lhy7;"));
        code.add(moveResultObject(0));
        code.add(new BuilderInstruction21c(Opcode.SPUT_OBJECT, 0,
                new ImmutableFieldReference(owner, "e", "Lhy7;")));
        code.add(new BuilderInstruction10x(Opcode.RETURN_VOID));
        return code;
    }

    private static RuntimeException fail(String message) {
        return new RuntimeException(new PatchException(message));
    }

    private static MutableMethod findMethod(BytecodePatchContext context, String type, String name, String ret) {
        MutableClass mutable = context.mutableClassDefByOrNull(type);
        if (mutable == null) {
            return null;
        }
        for (Method method : mutable.getMethods()) {
            if (name.equals(method.getName()) && ret.equals(method.getReturnType())) {
                return (MutableMethod) method;
            }
        }
        return null;
    }

    private static boolean hasString(Method method, String value) {
        if (method.getImplementation() == null) {
            return false;
        }
        for (Instruction instruction : method.getImplementation().getInstructions()) {
            if (!(instruction instanceof ReferenceInstruction)) {
                continue;
            }
            Object reference = ((ReferenceInstruction) instruction).getReference();
            if (reference instanceof StringReference
                    && value.equals(((StringReference) reference).getString())) {
                return true;
            }
        }
        return false;
    }

    private static BuilderInstruction sget(int reg, String owner, String name, String type) {
        return new BuilderInstruction21c(Opcode.SGET_OBJECT, reg, new ImmutableFieldReference(owner, name, type));
    }

    private static BuilderInstruction constString(int reg, String value) {
        return new BuilderInstruction21c(Opcode.CONST_STRING, reg, new ImmutableStringReference(value));
    }

    private static BuilderInstruction iput(Opcode opcode, int valueReg, int objectReg, String owner, String name, String type) {
        return new BuilderInstruction22c(opcode, valueReg, objectReg, new ImmutableFieldReference(owner, name, type));
    }

    private static BuilderInstruction moveResultObject(int reg) {
        return new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, reg);
    }

    private static BuilderInstruction invokeStatic(int count, int c, int d, int e, int f, int g,
                                                    String owner, String name, List<String> params, String ret) {
        return new BuilderInstruction35c(Opcode.INVOKE_STATIC, count, c, d, e, f, g,
                new ImmutableMethodReference(owner, name, params, ret));
    }

    private static BuilderInstruction invokeVirtual(int count, int c, int d, int e, int f, int g,
                                                     String owner, String name, List<String> params, String ret) {
        return new BuilderInstruction35c(Opcode.INVOKE_VIRTUAL, count, c, d, e, f, g,
                new ImmutableMethodReference(owner, name, params, ret));
    }

    private static BuilderInstruction invokeDirect(int count, int c, int d, int e, int f, int g,
                                                    String owner, String name, List<String> params, String ret) {
        return new BuilderInstruction35c(Opcode.INVOKE_DIRECT, count, c, d, e, f, g,
                new ImmutableMethodReference(owner, name, params, ret));
    }
}
