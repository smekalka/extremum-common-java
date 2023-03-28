* In commit dbcc5fa447cff587e7bcf28747b8dbe2ffe2aeca (version 1.2.9), `equals()`` and `hashCode()`` methods
  were removed from `Descriptor` class, because to make descriptors always comparable, sometimes you need
  to load a descriptor from database which is not a good idea during `equals()` or `hashCode()` invocation.
  This will only bite you if you put descriptors to hash collections or compare them manually using
  `equals()`/`hashCode()`. If you do, you should wrap descriptors in some kind of an 'explicit loader'.