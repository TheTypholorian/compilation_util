mixin<TargetClass> {
    inject(
        method = targetMethod<int>,
        at = invoke(SomeClass.methodCall)
    ) {
        if (arg == 10) {
            cancel 30f;
        }
    }
}