package ai.auralog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class AuralogConfigGlobalMetadataTest {

  @Test
  void defaultGlobalMetadataIsNull() {
    AuralogConfig config = AuralogConfig.builder().apiKey("k").build();
    assertThat(config.globalMetadata()).isNull();
  }

  @Test
  void supplierFormPreserved() {
    Supplier<Map<String, Object>> supplier = () -> Map.of("u", "1");
    AuralogConfig config = AuralogConfig.builder().apiKey("k").globalMetadata(supplier).build();
    assertThat(config.globalMetadata()).isSameAs(supplier);
  }

  @Test
  void mapOverloadWrappedAsSupplierReturningSameMap() {
    Map<String, Object> map = Map.of("u", "1");
    AuralogConfig config = AuralogConfig.builder().apiKey("k").globalMetadata(map).build();
    Supplier<Map<String, Object>> resolved = config.globalMetadata();
    assertThat(resolved).isNotNull();
    assertThat(resolved.get()).isEqualTo(map);
  }

  @Test
  void mapOverloadWithNullClearsConfig() {
    AuralogConfig config =
        AuralogConfig.builder().apiKey("k").globalMetadata((Map<String, Object>) null).build();
    assertThat(config.globalMetadata()).isNull();
  }

  @Test
  void supplierIsLateBound() {
    int[] counter = {0};
    Supplier<Map<String, Object>> supplier =
        () -> {
          counter[0]++;
          return Map.of("call", counter[0]);
        };
    AuralogConfig config = AuralogConfig.builder().apiKey("k").globalMetadata(supplier).build();
    // Building config should not invoke the supplier.
    assertThat(counter[0]).isZero();
    config.globalMetadata().get();
    assertThat(counter[0]).isEqualTo(1);
  }
}
