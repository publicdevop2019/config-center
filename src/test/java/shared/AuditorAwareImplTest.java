package shared;

import com.hw.shared.AuditorAwareImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class AuditorAwareImplTest {

    AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @Test
    public void getCurrentAuditor_noAuth() {
        Optional<String> currentAuditor = auditorAware.getCurrentAuditor();
        Assert.assertEquals(false, currentAuditor.isEmpty());
        Assert.assertEquals("NOT_HTTP", currentAuditor.get());
    }
}