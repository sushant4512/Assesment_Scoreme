package com.assignment.assignment.model;



import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Data
public class PdfDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storagePath;
    private int numberOfCuts;

    @ElementCollection
    private List<String> segmentedPaths;


}