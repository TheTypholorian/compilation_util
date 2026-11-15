package ...mixin;

import ...;

@Mixin(TargetClass.class)
public class TargetClassMixin {
    @Inject(
            method = "targetMethod(I)F",
            at = @At(
                    value = "INVOKE",
                    target = "LSomeClass;methodCall()V"
            )
    )
    private void injector(int arg, CallbackInfoReturnable<Float> cir) {
        if (arg == 10) {
            cir.setReturnValue(30f);
        }
    }
}