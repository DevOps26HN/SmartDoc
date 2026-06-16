package com.smartdoc.document.service;

import com.smartdoc.document.dto.*;
import com.smartdoc.document.model.Document;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final List<Document> documents = new ArrayList<>();

    public DocumentServiceImpl() {
        // Seed documents (W04H01 mock data). Stable UUID ids let the client
        // address a specific document for the GenAI summarize/ask features, and
        // the longer bodies make the AI output meaningful to demonstrate.
        documents.add(new Document(
            "11111111-1111-1111-1111-111111111111",
            "Attention Efficiency in Transformers (Research Paper)",
            """
            Abstract. Transformer models have become the backbone of modern natural language
            processing, but their self-attention mechanism scales quadratically with sequence
            length, which makes long-document processing expensive. In this paper we introduce
            BlockSparse Attention, a drop-in replacement that restricts each token to attend to a
            learned set of local and global blocks. On the Long Range Arena benchmark our method
            reaches 91.4% of full-attention accuracy while reducing memory use by 4.7x and
            increasing throughput by 3.1x on sequences of 8,000 tokens. We further show that
            BlockSparse Attention can be fine-tuned from existing pre-trained checkpoints in under
            two GPU-hours, making it practical for teams without large training budgets. Our
            ablation studies indicate that the global blocks are responsible for most of the
            retained accuracy, while the local blocks chiefly improve throughput. We release code
            and pre-trained weights to support reproducibility.
            """,
            "PROCESSED"));

        documents.add(new Document(
            "22222222-2222-2222-2222-222222222222",
            "Office Lease Agreement",
            """
            This Commercial Lease Agreement ("Agreement") is entered into between Northwind
            Properties LLC ("Landlord") and the Tenant for the premises located at 200 Market
            Street, Suite 4B.

            1. Term. The initial term of this lease is thirty-six (36) months, commencing on the
            first day of the month following execution.

            2. Rent. Tenant shall pay monthly base rent of 4,200 EUR, due on the first day of each
            calendar month. A late fee of 5% applies to payments received after the fifth day.

            3. Termination. Either party may terminate this Agreement by providing no less than
            sixty (60) days' prior written notice to the other party. Early termination by the
            Tenant without the required notice forfeits the security deposit.

            4. Maintenance. The Landlord is responsible for structural repairs; the Tenant is
            responsible for interior upkeep and any damage beyond ordinary wear and tear.
            """,
            "PROCESSED"));

        documents.add(new Document(
            "33333333-3333-3333-3333-333333333333",
            "RX-200 Router Setup Manual",
            """
            RX-200 Wireless Router — Quick Setup and Troubleshooting.

            Setup: Connect the WAN port to your modem, power on the unit, and wait for the status
            LED to turn solid green. Connect to the default SSID printed on the label and open
            http://192.168.0.1 to start the setup wizard.

            Troubleshooting error codes:
            - E-101: No WAN signal detected. Check the cable between the modem and the WAN port.
            - E-103: DHCP lease could not be obtained from the upstream provider. Power-cycle the
              modem, wait 60 seconds, then restart the router. If the error persists, contact your
              ISP to confirm the line is provisioned.
            - E-205: Firmware update failed. Re-download the firmware and retry over a wired
              connection; do not power off the device during an update.
            """,
            "PROCESSED"));
    }

    @Override
    public DocumentListResponseDto listDocuments(int page, int pageSize, String ownerEmail) {
        List<DocumentResponseDto> dtos = documents.stream()
            .map(d -> new DocumentResponseDto(
                UUID.fromString(d.getId()),
                d.getTitle(),
                "text/plain",
                (long) d.getContent().length(),
                d.getStatus().equals("PROCESSED") ? DocumentStatusDto.DONE : DocumentStatusDto.PENDING,
                1,
                "EN",
                OffsetDateTime.now(),
                null
            ))
            .collect(Collectors.toList());

        return new DocumentListResponseDto(
            dtos,
            dtos.size(),
            page,
            pageSize
        );
    }

    @Override
    public List<Document> getAllDocuments() {
        return documents;
    }

    @Override
    public Optional<Document> getDocumentById(String id) {
        return documents.stream().filter(d -> d.getId().equals(id)).findFirst();
    }
}
