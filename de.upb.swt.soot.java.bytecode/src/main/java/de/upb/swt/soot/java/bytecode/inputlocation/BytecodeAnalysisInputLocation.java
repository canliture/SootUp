package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

public interface BytecodeAnalysisInputLocation extends AnalysisInputLocation {

  @Nonnull
  @Override
  default Optional<? extends AbstractClassSource> getClassSource(@Nonnull JavaClassType type) {
    return getClassSource(type, BytecodeClassLoadingOptions.Default);
  }

  @Nonnull
  @Override
  default Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, BytecodeClassLoadingOptions.Default);
  }
}