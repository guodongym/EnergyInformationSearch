package com.cnpc.geo.energyinformationsearch.kafka;

import kafka.serializer.Decoder;

public class KeyDecoder<T> implements Decoder<T> {
	

	@SuppressWarnings("unchecked")
	@Override
	public T fromBytes(byte[] arg0) {
		if (arg0!=null) {
			return (T) SerializUtils.ByteToObject(arg0);
		}
		return null;
	}

}
