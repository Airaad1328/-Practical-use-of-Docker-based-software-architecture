package com.gmail.clarkin200.LayeredMonolith.application;

import java.util.List;
import java.util.Optional;

public interface BaseService <T,V>{
    Optional<V> save (V entity);
    Optional<V> getById (T id);
    List<V> fetchAll ();
}
