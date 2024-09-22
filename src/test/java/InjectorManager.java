import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectorManager {
    private static Injector injector;


    public static Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new TestModule(), new ConfigModule());
        }
        return injector;
    }
}
