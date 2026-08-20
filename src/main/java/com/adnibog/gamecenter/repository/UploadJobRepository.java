package com.adnibog.gamecenter.repository;

import java.util.Optional;
import com.adnibog.gamecenter.entity.UploadJob;

public interface UploadJobRepository {
  Optional<UploadJob> findById(String id);

  void save(UploadJob job);
}
