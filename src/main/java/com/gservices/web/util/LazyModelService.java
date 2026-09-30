package com.gservices.web.util;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Adaptateur générique entre un service paginé Spring Data
 * ({@code (filtre, Pageable) -> Page<T>}) et le {@link LazyDataModel} de
 * PrimeFaces : pagination / tri côté serveur, filtre texte global.
 *
 * @param <T> type du DTO affiché dans la {@code p:dataTable}
 */
public class LazyModelService<T> extends LazyDataModel<T> {

    @FunctionalInterface
    public interface Finder<T> {
        Page<T> chercher(String filtre, Pageable pageable);
    }

    private final transient Finder<T> finder;
    private final transient Function<T, ?> idExtractor;
    private final transient Supplier<String> filtreSupplier;
    private transient List<T> pageCourante = List.of();

    public LazyModelService(Finder<T> finder, Function<T, ?> idExtractor, Supplier<String> filtreSupplier) {
        this.finder = finder;
        this.idExtractor = idExtractor;
        this.filtreSupplier = filtreSupplier;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) finder.chercher(filtre(), PageRequest.of(0, 1)).getTotalElements();
    }

    @Override
    public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        int taille = Math.max(pageSize, 1);
        int page = first / taille;
        Page<T> resultat = finder.chercher(filtre(), PageRequest.of(page, taille, toSort(sortBy)));
        setRowCount((int) resultat.getTotalElements());
        this.pageCourante = resultat.getContent();
        return this.pageCourante;
    }

    @Override
    public String getRowKey(T object) {
        return object == null ? null : String.valueOf(idExtractor.apply(object));
    }

    @Override
    public T getRowData(String rowKey) {
        if (rowKey == null) {
            return null;
        }
        return pageCourante.stream()
                .filter(o -> rowKey.equals(String.valueOf(idExtractor.apply(o))))
                .findFirst()
                .orElse(null);
    }

    private String filtre() {
        return filtreSupplier == null ? null : filtreSupplier.get();
    }

    private static Sort toSort(Map<String, SortMeta> sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> ordres = new ArrayList<>();
        for (SortMeta sm : sortBy.values()) {
            if (sm == null || sm.getField() == null
                    || sm.getOrder() == null || sm.getOrder() == SortOrder.UNSORTED) {
                continue;
            }
            ordres.add(sm.getOrder() == SortOrder.ASCENDING
                    ? Sort.Order.asc(sm.getField())
                    : Sort.Order.desc(sm.getField()));
        }
        return ordres.isEmpty() ? Sort.unsorted() : Sort.by(ordres);
    }
}
