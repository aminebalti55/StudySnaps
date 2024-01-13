package com.example.studysnaps.services;

import com.example.studysnaps.Repositories.DocumentTagRepository;
import com.example.studysnaps.Repositories.TagRepository;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.entities.DocumentTag;
import com.example.studysnaps.entities.PDFDocument;
import com.example.studysnaps.entities.Tag;

import com.example.studysnaps.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentTagRepository documentTagRepository;

    public List<Tag> getOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = userDetails.getUsername();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName);

            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tag.setDocuments(new ArrayList<>());
            }

            // Associate each uploaded document with the tag
            List<PDFDocument> uploadedDocuments = user.getUploadedDocuments();
            if (uploadedDocuments != null && !uploadedDocuments.isEmpty()) {
                for (PDFDocument document : uploadedDocuments) {
                    tag.getDocuments().add(document);
                }

                DocumentTag documentTag = new DocumentTag();
                documentTag.setPdfDocument(uploadedDocuments.get(0));
                documentTag.setUserId(user.getUserId());
                documentTagRepository.save(documentTag);
            }

            tagRepository.save(tag);

            tags.add(tag);
        }

        return tags;
    }

}
