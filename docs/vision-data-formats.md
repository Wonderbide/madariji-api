# Formats de Données Vision API

## Format Vision API Natif (GCS Output)

Structure retournée par Google Vision API dans GCS après traitement OCR:

```json
{
  "responses": [
    {
      "fullTextAnnotation": {
        "text": "Texte complet de la page extrait par OCR...",
        "pages": [
          {
            "property": {...},
            "width": 2550,
            "height": 3300,
            "blocks": [...]
          }
        ]
      },
      "context": {
        "pageNumber": 1
      }
    }
  ]
}
```

## Format Transformé (VisionBatchData)

Format utilisé en interne après transformation par VisionResultPoller:

```json
{
  "bookId": "831fe7e8-d8be-40a6-9bb2-93003b7e05e7",
  "totalPages": 150,
  "timestamp": "2025-01-20T10:30:00Z",
  "pages": [
    {
      "pageNumber": 1,
      "text": "Texte extrait de la page 1..."
    },
    {
      "pageNumber": 2,
      "text": "Texte extrait de la page 2..."
    }
  ]
}
```

## Classes Java Correspondantes

### VisionBatchData.java
```java
public class VisionBatchData {
    private String bookId;
    private int totalPages;
    private List<VisionPage> pages;
    private Date timestamp;
}
```

### VisionPage.java
```java
public class VisionPage {
    private int pageNumber;
    private String text;
}
```

## Points Importants

1. **Vision API** génère plusieurs fichiers JSON (1 par batch de 100 pages)
2. **VisionResultPoller** consolide tous ces fichiers en un seul
3. **CloudRequestFormatter** s'attend au format transformé (VisionBatchData)
4. Le nouveau système devra gérer cette transformation dans le listener