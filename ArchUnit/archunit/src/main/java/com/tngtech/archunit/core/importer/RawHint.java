/*
 * Copyright 2014-2020 TNG Technology Consulting GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tngtech.archunit.core.importer;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaType;

import java.util.Objects;

public class RawHint {
    private JavaType type;
    private JavaType memberOwner;
    private String memberName;
    private String memberDescriptor;

    public RawHint(JavaType type) {
        this.type = type;
    }

    public RawHint(JavaType type, JavaType owner, String name, String descriptor) {
        this(type);
        this.memberOwner = owner;
        this.memberName = name;
        this.memberDescriptor = descriptor;
    }

    public JavaType getType() {
        return type;
    }

    public JavaType getMemberOwner() {
        return memberOwner;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberDescriptor() {
        return memberDescriptor;
    }

    public boolean hasMember() {
        return memberOwner != null;
    }

    public JavaMember resolveMemberIn(JavaClass targetClass) {
        try {
            for (JavaMember member : targetClass.getAllMembers()) {
                if (memberName.equals(member.getName()) && memberDescriptor.equals(member.getDescriptor())) {
                    return member;
                }
            }

            // Member is inherited from Object; it's fine to return null
        } catch (NullPointerException e) {
            System.err.println("Tried to get members before they have been processed, raw hint: " + this);
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawHint rawHint = (RawHint) o;
        return type.equals(rawHint.type) &&
                Objects.equals(memberOwner, rawHint.memberOwner) &&
                Objects.equals(memberName, rawHint.memberName) &&
                Objects.equals(memberDescriptor, rawHint.memberDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, memberOwner, memberName, memberDescriptor);
    }

    @Override
    public String toString() {
        return "RawHint{" +
                "type=" + type +
                ", memberOwner=" + memberOwner +
                ", memberName='" + memberName + '\'' +
                ", memberDescriptor='" + memberDescriptor + '\'' +
                '}';
    }
}
