/*
 * Copyright 2013 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.exhibitor.core.s3;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class S3ClientImpl implements S3Client
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicReference<RefCountedClient> client = new AtomicReference<RefCountedClient>(null);
    private final String s3Region;

    public S3ClientImpl(S3Credential credentials, String s3Region)
    {
        this.s3Region = s3Region;
        changeCredentials(credentials);
    }

    public S3ClientImpl(S3Credential credentials, S3ClientConfig clientConfig, String s3Region)
    {
        this.s3Region = s3Region;
        changeCredentials(credentials, clientConfig);
    }

    public S3ClientImpl(S3CredentialsProvider credentialsProvider, String s3Region)
    {
        this.s3Region = s3Region;
        client.set(new RefCountedClient(createClient(credentialsProvider.getAWSCredentialProvider(), null, null)));
    }

    public S3ClientImpl(S3CredentialsProvider credentialsProvider, S3ClientConfig clientConfig, String s3Region)
    {
        this.s3Region = s3Region;
        client.set(new RefCountedClient(createClient(credentialsProvider.getAWSCredentialProvider(), null, clientConfig)));
    }

    @Override
    public void changeCredentials(S3Credential credential)
    {
        RefCountedClient   newRefCountedClient = (credential != null) ? new RefCountedClient(createClient(null, new BasicAWSCredentials(credential.getAccessKeyId(), credential.getSecretAccessKey()), null)) : new RefCountedClient(createClient(null, null, null));
        RefCountedClient   oldRefCountedClient = client.getAndSet(newRefCountedClient);
        if ( oldRefCountedClient != null )
        {
            oldRefCountedClient.markForDelete();
        }
    }

    @Override
    public void changeCredentials(S3Credential credential, S3ClientConfig clientConfig)
    {
        RefCountedClient   newRefCountedClient = (credential != null) ? new RefCountedClient(createClient(null, new BasicAWSCredentials(credential.getAccessKeyId(), credential.getSecretAccessKey()), clientConfig)) : new RefCountedClient(createClient(null, null, clientConfig));
        RefCountedClient   oldRefCountedClient = client.getAndSet(newRefCountedClient);
        if ( oldRefCountedClient != null )
        {
            oldRefCountedClient.markForDelete();
        }
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            changeCredentials(null, null);
        }
        catch ( Exception e )
        {
            throw new IOException(e);
        }
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(InitiateMultipartUploadRequest request) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.initiateMultipartUpload(request);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest request) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.putObject(request);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public S3Object getObject(String bucket, String key) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.getObject(bucket, key);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucket, String key) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.getObjectMetadata(bucket, key);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest request) throws Exception
    {
        RefCountedClient    holder = client.get();
        AmazonS3Client      amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.listObjects(request);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing previousObjectListing) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.listNextBatchOfObjects(previousObjectListing);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public void deleteObject(String bucket, String key) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            amazonS3Client.deleteObject(bucket, key);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            return amazonS3Client.uploadPart(request);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public void completeMultipartUpload(CompleteMultipartUploadRequest request) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            amazonS3Client.completeMultipartUpload(request);
        }
        finally
        {
            holder.release();
        }
    }

    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest request) throws Exception
    {
        RefCountedClient holder = client.get();
        AmazonS3Client amazonS3Client = holder.useClient();
        try
        {
            amazonS3Client.abortMultipartUpload(request);
        }
        finally
        {
            holder.release();
        }
    }

    private AmazonS3Client createClient(AWSCredentialsProvider awsCredentialProvider, BasicAWSCredentials basicAWSCredentials, S3ClientConfig clientConfig)
    {
        AmazonS3Client localClient;

        // Setting endpoint property will always supersede region setting.
        String endpoint = System.getProperty("exhibitor-s3-endpoint");

        if ( awsCredentialProvider != null )
        {
            if ( clientConfig != null )
            {
                localClient = new AmazonS3Client(awsCredentialProvider, clientConfig.getAWSClientConfig());
            }
            else
            {
                localClient = new AmazonS3Client(awsCredentialProvider);
            }
        }
        else if ( basicAWSCredentials != null )
        {
            if ( clientConfig != null )
            {
                localClient = new AmazonS3Client(basicAWSCredentials, clientConfig.getAWSClientConfig());
            }
            else
            {
                localClient = new AmazonS3Client(basicAWSCredentials);
            }
        }
        else
        {
            if ( clientConfig != null )
            {
                localClient = new AmazonS3Client(clientConfig.getAWSClientConfig());
            }
            else
            {
                localClient = new AmazonS3Client();
            }
        }

        if ( endpoint != null )
        {
            localClient.setEndpoint(endpoint);
            log.info("Setting S3 endpoint to: " + endpoint);
        }
        else if ( s3Region != null)
        {
            localClient.setRegion(RegionUtils.getRegion(s3Region));
            log.info("Setting S3 region to: " + s3Region);
        }

        return localClient;
    }
}

