package com.adityachandel.booklore.repository;

import com.adityachandel.booklore.model.entity.PdfViewerPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfViewerPreferencesRepository extends JpaRepository<PdfViewerPreferencesEntity, Long> {

}
