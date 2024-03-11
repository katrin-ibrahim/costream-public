/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package org.apache.storm.generated;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)")
public class HBMessage implements org.apache.storm.thrift.TBase<HBMessage, HBMessage._Fields>, java.io.Serializable, Cloneable, Comparable<HBMessage> {
  private static final org.apache.storm.thrift.protocol.TStruct STRUCT_DESC = new org.apache.storm.thrift.protocol.TStruct("HBMessage");

  private static final org.apache.storm.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.storm.thrift.protocol.TField("type", org.apache.storm.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.storm.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.storm.thrift.protocol.TField("data", org.apache.storm.thrift.protocol.TType.STRUCT, (short)2);
  private static final org.apache.storm.thrift.protocol.TField MESSAGE_ID_FIELD_DESC = new org.apache.storm.thrift.protocol.TField("message_id", org.apache.storm.thrift.protocol.TType.I32, (short)3);

  private static final org.apache.storm.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new HBMessageStandardSchemeFactory();
  private static final org.apache.storm.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new HBMessageTupleSchemeFactory();

  private @org.apache.storm.thrift.annotation.Nullable HBServerMessageType type; // required
  private @org.apache.storm.thrift.annotation.Nullable HBMessageData data; // required
  private int message_id; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.storm.thrift.TFieldIdEnum {
    /**
     * 
     * @see HBServerMessageType
     */
    TYPE((short)1, "type"),
    DATA((short)2, "data"),
    MESSAGE_ID((short)3, "message_id");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.storm.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TYPE
          return TYPE;
        case 2: // DATA
          return DATA;
        case 3: // MESSAGE_ID
          return MESSAGE_ID;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.storm.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MESSAGE_ID_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.MESSAGE_ID};
  public static final java.util.Map<_Fields, org.apache.storm.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.storm.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.storm.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TYPE, new org.apache.storm.thrift.meta_data.FieldMetaData("type", org.apache.storm.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.storm.thrift.meta_data.EnumMetaData(org.apache.storm.thrift.protocol.TType.ENUM, HBServerMessageType.class)));
    tmpMap.put(_Fields.DATA, new org.apache.storm.thrift.meta_data.FieldMetaData("data", org.apache.storm.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.storm.thrift.meta_data.StructMetaData(org.apache.storm.thrift.protocol.TType.STRUCT, HBMessageData.class)));
    tmpMap.put(_Fields.MESSAGE_ID, new org.apache.storm.thrift.meta_data.FieldMetaData("message_id", org.apache.storm.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.storm.thrift.meta_data.FieldValueMetaData(org.apache.storm.thrift.protocol.TType.I32)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.storm.thrift.meta_data.FieldMetaData.addStructMetaDataMap(HBMessage.class, metaDataMap);
  }

  public HBMessage() {
    this.message_id = -1;

  }

  public HBMessage(
    HBServerMessageType type,
    HBMessageData data)
  {
    this();
    this.type = type;
    this.data = data;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public HBMessage(HBMessage other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.is_set_type()) {
      this.type = other.type;
    }
    if (other.is_set_data()) {
      this.data = new HBMessageData(other.data);
    }
    this.message_id = other.message_id;
  }

  public HBMessage deepCopy() {
    return new HBMessage(this);
  }

  @Override
  public void clear() {
    this.type = null;
    this.data = null;
    this.message_id = -1;

  }

  /**
   * 
   * @see HBServerMessageType
   */
  @org.apache.storm.thrift.annotation.Nullable
  public HBServerMessageType get_type() {
    return this.type;
  }

  /**
   * 
   * @see HBServerMessageType
   */
  public void set_type(@org.apache.storm.thrift.annotation.Nullable HBServerMessageType type) {
    this.type = type;
  }

  public void unset_type() {
    this.type = null;
  }

  /** Returns true if field type is set (has been assigned a value) and false otherwise */
  public boolean is_set_type() {
    return this.type != null;
  }

  public void set_type_isSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }

  @org.apache.storm.thrift.annotation.Nullable
  public HBMessageData get_data() {
    return this.data;
  }

  public void set_data(@org.apache.storm.thrift.annotation.Nullable HBMessageData data) {
    this.data = data;
  }

  public void unset_data() {
    this.data = null;
  }

  /** Returns true if field data is set (has been assigned a value) and false otherwise */
  public boolean is_set_data() {
    return this.data != null;
  }

  public void set_data_isSet(boolean value) {
    if (!value) {
      this.data = null;
    }
  }

  public int get_message_id() {
    return this.message_id;
  }

  public void set_message_id(int message_id) {
    this.message_id = message_id;
    set_message_id_isSet(true);
  }

  public void unset_message_id() {
    __isset_bitfield = org.apache.storm.thrift.EncodingUtils.clearBit(__isset_bitfield, __MESSAGE_ID_ISSET_ID);
  }

  /** Returns true if field message_id is set (has been assigned a value) and false otherwise */
  public boolean is_set_message_id() {
    return org.apache.storm.thrift.EncodingUtils.testBit(__isset_bitfield, __MESSAGE_ID_ISSET_ID);
  }

  public void set_message_id_isSet(boolean value) {
    __isset_bitfield = org.apache.storm.thrift.EncodingUtils.setBit(__isset_bitfield, __MESSAGE_ID_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.storm.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case TYPE:
      if (value == null) {
        unset_type();
      } else {
        set_type((HBServerMessageType)value);
      }
      break;

    case DATA:
      if (value == null) {
        unset_data();
      } else {
        set_data((HBMessageData)value);
      }
      break;

    case MESSAGE_ID:
      if (value == null) {
        unset_message_id();
      } else {
        set_message_id((java.lang.Integer)value);
      }
      break;

    }
  }

  @org.apache.storm.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case TYPE:
      return get_type();

    case DATA:
      return get_data();

    case MESSAGE_ID:
      return get_message_id();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case TYPE:
      return is_set_type();
    case DATA:
      return is_set_data();
    case MESSAGE_ID:
      return is_set_message_id();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof HBMessage)
      return this.equals((HBMessage)that);
    return false;
  }

  public boolean equals(HBMessage that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_type = true && this.is_set_type();
    boolean that_present_type = true && that.is_set_type();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }

    boolean this_present_data = true && this.is_set_data();
    boolean that_present_data = true && that.is_set_data();
    if (this_present_data || that_present_data) {
      if (!(this_present_data && that_present_data))
        return false;
      if (!this.data.equals(that.data))
        return false;
    }

    boolean this_present_message_id = true && this.is_set_message_id();
    boolean that_present_message_id = true && that.is_set_message_id();
    if (this_present_message_id || that_present_message_id) {
      if (!(this_present_message_id && that_present_message_id))
        return false;
      if (this.message_id != that.message_id)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((is_set_type()) ? 131071 : 524287);
    if (is_set_type())
      hashCode = hashCode * 8191 + type.getValue();

    hashCode = hashCode * 8191 + ((is_set_data()) ? 131071 : 524287);
    if (is_set_data())
      hashCode = hashCode * 8191 + data.hashCode();

    hashCode = hashCode * 8191 + ((is_set_message_id()) ? 131071 : 524287);
    if (is_set_message_id())
      hashCode = hashCode * 8191 + message_id;

    return hashCode;
  }

  @Override
  public int compareTo(HBMessage other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(is_set_type()).compareTo(other.is_set_type());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_type()) {
      lastComparison = org.apache.storm.thrift.TBaseHelper.compareTo(this.type, other.type);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_data()).compareTo(other.is_set_data());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_data()) {
      lastComparison = org.apache.storm.thrift.TBaseHelper.compareTo(this.data, other.data);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(is_set_message_id()).compareTo(other.is_set_message_id());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_message_id()) {
      lastComparison = org.apache.storm.thrift.TBaseHelper.compareTo(this.message_id, other.message_id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.storm.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.storm.thrift.protocol.TProtocol iprot) throws org.apache.storm.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.storm.thrift.protocol.TProtocol oprot) throws org.apache.storm.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("HBMessage(");
    boolean first = true;

    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("data:");
    if (this.data == null) {
      sb.append("null");
    } else {
      sb.append(this.data);
    }
    first = false;
    if (is_set_message_id()) {
      if (!first) sb.append(", ");
      sb.append("message_id:");
      sb.append(this.message_id);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.storm.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.storm.thrift.protocol.TCompactProtocol(new org.apache.storm.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.storm.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.storm.thrift.protocol.TCompactProtocol(new org.apache.storm.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.storm.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class HBMessageStandardSchemeFactory implements org.apache.storm.thrift.scheme.SchemeFactory {
    public HBMessageStandardScheme getScheme() {
      return new HBMessageStandardScheme();
    }
  }

  private static class HBMessageStandardScheme extends org.apache.storm.thrift.scheme.StandardScheme<HBMessage> {

    public void read(org.apache.storm.thrift.protocol.TProtocol iprot, HBMessage struct) throws org.apache.storm.thrift.TException {
      org.apache.storm.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.storm.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TYPE
            if (schemeField.type == org.apache.storm.thrift.protocol.TType.I32) {
              struct.type = org.apache.storm.generated.HBServerMessageType.findByValue(iprot.readI32());
              struct.set_type_isSet(true);
            } else { 
              org.apache.storm.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DATA
            if (schemeField.type == org.apache.storm.thrift.protocol.TType.STRUCT) {
              struct.data = new HBMessageData();
              struct.data.read(iprot);
              struct.set_data_isSet(true);
            } else { 
              org.apache.storm.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // MESSAGE_ID
            if (schemeField.type == org.apache.storm.thrift.protocol.TType.I32) {
              struct.message_id = iprot.readI32();
              struct.set_message_id_isSet(true);
            } else { 
              org.apache.storm.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.storm.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.storm.thrift.protocol.TProtocol oprot, HBMessage struct) throws org.apache.storm.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.type.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.data != null) {
        oprot.writeFieldBegin(DATA_FIELD_DESC);
        struct.data.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_message_id()) {
        oprot.writeFieldBegin(MESSAGE_ID_FIELD_DESC);
        oprot.writeI32(struct.message_id);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class HBMessageTupleSchemeFactory implements org.apache.storm.thrift.scheme.SchemeFactory {
    public HBMessageTupleScheme getScheme() {
      return new HBMessageTupleScheme();
    }
  }

  private static class HBMessageTupleScheme extends org.apache.storm.thrift.scheme.TupleScheme<HBMessage> {

    @Override
    public void write(org.apache.storm.thrift.protocol.TProtocol prot, HBMessage struct) throws org.apache.storm.thrift.TException {
      org.apache.storm.thrift.protocol.TTupleProtocol oprot = (org.apache.storm.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.is_set_type()) {
        optionals.set(0);
      }
      if (struct.is_set_data()) {
        optionals.set(1);
      }
      if (struct.is_set_message_id()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.is_set_type()) {
        oprot.writeI32(struct.type.getValue());
      }
      if (struct.is_set_data()) {
        struct.data.write(oprot);
      }
      if (struct.is_set_message_id()) {
        oprot.writeI32(struct.message_id);
      }
    }

    @Override
    public void read(org.apache.storm.thrift.protocol.TProtocol prot, HBMessage struct) throws org.apache.storm.thrift.TException {
      org.apache.storm.thrift.protocol.TTupleProtocol iprot = (org.apache.storm.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.type = org.apache.storm.generated.HBServerMessageType.findByValue(iprot.readI32());
        struct.set_type_isSet(true);
      }
      if (incoming.get(1)) {
        struct.data = new HBMessageData();
        struct.data.read(iprot);
        struct.set_data_isSet(true);
      }
      if (incoming.get(2)) {
        struct.message_id = iprot.readI32();
        struct.set_message_id_isSet(true);
      }
    }
  }

  private static <S extends org.apache.storm.thrift.scheme.IScheme> S scheme(org.apache.storm.thrift.protocol.TProtocol proto) {
    return (org.apache.storm.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

